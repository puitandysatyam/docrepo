package org.project.docrepo.controllers;

import jakarta.validation.Valid;
import org.project.docrepo.model.DocumentDto;
import org.project.docrepo.model.Documents;
import org.project.docrepo.model.User;
import org.project.docrepo.repo.DocumentRepo;
import org.project.docrepo.services.GoogleDriveService;
import org.project.docrepo.services.RequestService;
import org.project.docrepo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.List;


@Controller
public class DocumentController {

    @Autowired
    GoogleDriveService googleDriveService;

    private final DocumentRepo documentRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private RequestService requestService;

    public DocumentController(DocumentRepo documentRepo) {
        this.documentRepo = documentRepo;
    }

    @GetMapping("/documents/upload")
    public String showDocumentUploadForm(Model model) {
        model.addAttribute("documentDto", new DocumentDto());
        return "upload-form";
    }

    @PostMapping("/documents/upload")
    public String uploadDocument(@Valid @ModelAttribute("documentDto") DocumentDto documentDto,
                                 BindingResult bindingResult,
                                 @RequestParam("file") MultipartFile file,
                                 @AuthenticationPrincipal User user,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {


        if (bindingResult.hasErrors()) {
            System.err.println("Validation errors found!");
            for (FieldError error : bindingResult.getFieldErrors()) {
                System.err.println(error.getField() + " - " + error.getDefaultMessage());
            }
            return "upload-form";
        }
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a file to upload!");
            return "redirect:/documents/upload";
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String safeFilename = (originalFilename == null) ? "file" : originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            String uniqueFileName = user.getDepartment() + "_" + user.getFullName() + "_" + safeFilename;

            String driveFileId = googleDriveService.uploadFile(file, uniqueFileName);

            Documents document = new Documents();
            document.setTitle(documentDto.getTitle());
            document.setDescription(documentDto.getDescription());
            document.setTopic(documentDto.getTopic());
            document.setFacultyId(user.getId());
            document.setFacultyName(user.getFullName());
            document.setDriveFileId(driveFileId);
            document.setUploadDate(LocalDate.now().toString());
            documentRepo.save(document);

        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "Upload failed: " + e.getMessage();
            model.addAttribute("errorMessage", errorMessage);
            return "upload-form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Document uploaded successfully!");
        return "redirect:/";
    }

    @GetMapping("/search")
    public String searchDocuments(@RequestParam String query, Model model){

        List<Documents> documents = documentRepo.searchByTopicTitleOrFaculty(query);
        model.addAttribute("documents",documents);
        model.addAttribute("pageTitle", "Search results for "+query);

        return "document-list";
    }

    @GetMapping("/documents/{id}")
    public String showDocumentDetails(Model model, @PathVariable String id, @AuthenticationPrincipal User user){

        Documents doc = documentRepo.findById(id).orElse(null);
        User faculty = userService.findUserById(doc.getFacultyId()).orElse(null);
        String previewUrl = "https://drive.google.com/file/d/" + doc.getDriveFileId() + "/preview";
        model.addAttribute("document", doc);
        model.addAttribute("faculty", faculty);
        model.addAttribute("previewUrl", previewUrl);
        model.addAttribute("currentUser", user);

        return "document-details";
    }


    // --- NEW METHOD FOR HANDLING FILE DOWNLOADS ---

    /**
     * Handles the secure download of a document.
     * @param documentId The ID of the document from our database.
     * @param currentUser The currently authenticated user, injected by Spring Security.
     * @return A ResponseEntity that streams the file data to the user's browser.
     */
    @GetMapping("/documents/{id}/download")
    public ResponseEntity<InputStreamResource> downloadDocument(@PathVariable("id") String documentId,
                                                                @AuthenticationPrincipal User currentUser) {

        // Step 1: CRITICAL SECURITY CHECK
        // We verify that the user is logged in AND their 'allowedDocumentsId' list contains this document's ID.
        if (currentUser == null || !currentUser.getAllowedDocumentsId().contains(documentId)) {
            // If the check fails, we deny access. Spring Security will typically show a 403 Forbidden page.
            throw new AccessDeniedException("You are not authorized to download this document.");
        }

        try {
            // Step 2: Fetch the document's metadata from our database to get the Google Drive file ID.
            Documents document = documentRepo.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document metadata not found in database."));

            // Step 3: Call our service to download the file's content from Google Drive.
            GoogleDriveService.DriveFile driveFile = googleDriveService.downloadFile(document.getDriveFileId());

            // Step 4: Prepare the file to be sent to the user's browser.
            InputStreamResource resource = new InputStreamResource(driveFile.inputStream());

            // Step 5: Set the HTTP headers. This tells the browser to open a "Save As..." dialog.
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + driveFile.name() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, driveFile.mimeType());

            // Step 6: Build and return the final response, streaming the file data.
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (IOException | GeneralSecurityException e) {
            // If anything goes wrong (e.g., file not found on Google Drive), we print the error and return an error status.
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/documents/{id}/delete")
    public String showDeleteConfirmation(@PathVariable String id, Model model){

        Documents doc =  documentRepo.findById(id).orElse(null);
        model.addAttribute("document", doc);
        return "delete-confirm";

    }

    @PostMapping("/documents/{id}/delete")
    public String deleteDocument(@PathVariable String id, RedirectAttributes redirectAttributes){
        try{
            documentRepo.deleteById(id);
            requestService.deleteRequestByDocId(id);
            redirectAttributes.addFlashAttribute("successMessage", "Successfully deleted the document.");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("Error deleting the document. Please try again later.");
            return "redirct:/profile";
        }
    }
}