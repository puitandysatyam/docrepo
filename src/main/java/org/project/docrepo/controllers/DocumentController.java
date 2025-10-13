package org.project.docrepo.controllers;

import jakarta.validation.Valid;
import org.project.docrepo.model.DocumentDto;
import org.project.docrepo.model.Documents;
import org.project.docrepo.model.User;
import org.project.docrepo.repo.DocumentRepo;
import org.project.docrepo.services.B2StorageService;
import org.project.docrepo.services.RequestService;
import org.project.docrepo.services.UserService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
public class DocumentController {

    // --- DEPENDENCIES ---
    // All services are now declared as 'final' and injected via the constructor.
    private final B2StorageService b2StorageService;
    private final DocumentRepo documentRepo;
    private final UserService userService;
    private final RequestService requestService;

    /**
     * Best Practice: Using Constructor Injection.
     * Spring will automatically provide the required service beans. This is cleaner,
     * safer, and easier to test than using @Autowired on each field.
     */
    public DocumentController(B2StorageService b2StorageService, DocumentRepo documentRepo, UserService userService, RequestService requestService) {
        this.b2StorageService = b2StorageService;
        this.documentRepo = documentRepo;
        this.userService = userService;
        this.requestService = requestService;
    }

    // --- UPLOAD MAPPING (Corrected & Refined) ---
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
            return "upload-form";
        }
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a file to upload!");
            return "redirect:/documents/upload";
        }

        try {
            // Generate a unique file name to prevent conflicts in the B2 bucket.
            // Using UUID is a robust way to ensure every file name is unique.
            String uniqueFileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");

            // Call the B2 service to upload the file and get the public URL.
            String fileUrl = b2StorageService.uploadFile(file, uniqueFileName);

            Documents document = new Documents();
            document.setTitle(documentDto.getTitle());
            document.setDescription(documentDto.getDescription());
            document.setTopic(documentDto.getTopic());
            document.setFacultyId(user.getId());
            document.setFacultyName(user.getFullName());
            // ** IMPORTANT **: Store the public URL, not the file ID.
            document.setFileUrl(fileUrl);
            document.setUploadDate(LocalDate.now().toString());
            documentRepo.save(document);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Upload failed: " + e.getMessage());
            model.addAttribute("documentDto", documentDto); // Add the DTO back to preserve user input
            return "upload-form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Document uploaded successfully!");
        return "redirect:/";
    }

    // --- SEARCH MAPPING (No Changes) ---
    @GetMapping("/search")
    public String searchDocuments(@RequestParam String query, Model model){
        List<Documents> documents = documentRepo.searchByTopicTitleOrFaculty(query);
        model.addAttribute("documents", documents);
        model.addAttribute("pageTitle", "Search results for " + query);
        return "document-list";
    }

    // --- DOCUMENT DETAILS MAPPING (Corrected) ---
    @GetMapping("/documents/{id}")
    public String showDocumentDetails(Model model, @PathVariable String id, @AuthenticationPrincipal User user){
        Documents doc = documentRepo.findById(id).orElse(null);
        if (doc == null) { throw new RuntimeException("Document not found"); }

        User faculty = userService.findUserById(doc.getFacultyId()).orElse(null);
        String fileName = doc.getFileUrl().substring(doc.getFileUrl().lastIndexOf('/') + 1);
        // ** KEY CHANGE **: For B2, the file URL is the preview URL for browser-compatible files like PDFs.
        // We no longer need to construct a special Google Drive URL.
        model.addAttribute("document", doc);
        model.addAttribute("faculty", faculty);
        model.addAttribute("previewUrl", b2StorageService.generatePresignedUrl(fileName) + "#toolbar=0"); // Use the direct file URL for the preview.
        model.addAttribute("currentUser", user);

        return "document-details";
    }

    // --- DOWNLOAD MAPPING (Corrected) ---
    @GetMapping("/documents/{id}/download")
    public ResponseEntity<InputStreamResource> downloadDocument(@PathVariable("id") String documentId,
                                                                @AuthenticationPrincipal User currentUser) {

        if (currentUser == null || !currentUser.getAllowedDocumentsId().contains(documentId)) {
            throw new AccessDeniedException("You are not authorized to download this document.");
        }

        Documents document = documentRepo.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document metadata not found in database."));

        // ** KEY CHANGE **: The B2 service needs the file's KEY/NAME, not its full URL.
        // We must extract the file name from the URL before calling the service.
        String fileUrl = document.getFileUrl();
        String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);

        // Call the B2 service to download the file by its name.
        B2StorageService.B2File b2file = b2StorageService.downloadFile(fileName);

        InputStreamResource resource = new InputStreamResource(b2file.inputStream());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + b2file.fileName() + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, b2file.contentType());

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    // --- DELETE MAPPINGS (Corrected & Secured) ---
    @GetMapping("/documents/{id}/delete")
    public String showDeleteConfirmPage(@PathVariable String id, @AuthenticationPrincipal User currentUser, Model model) {
        Documents document = documentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Security Check: Ensure the user owns this document before showing the delete page.
        if (!document.getFacultyId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this document.");
        }

        model.addAttribute("document", document);
        return "delete-confirm";
    }

    @PostMapping("/documents/{id}/delete")
    public String deleteDocument(@PathVariable String id, @AuthenticationPrincipal User currentUser, RedirectAttributes redirectAttributes) {
        try {
            Documents document = documentRepo.findById(id)
                    .orElseThrow(() -> new IllegalStateException("Document not found."));

            // Security Check: A final check to ensure the user is the owner.
            if (!document.getFacultyId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You are not authorized to delete this document.");
            }

            // Extract the file name from the URL.
            String fileUrl = document.getFileUrl();
            String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);

            // ** KEY CHANGE **: Call the service to delete the file from Backblaze B2.
            b2StorageService.deleteFile(fileName);

            // Delete the document record from our database.
            documentRepo.delete(document);

            // Delete all associated download requests.
            requestService.deleteRequestByDocId(id);

            redirectAttributes.addFlashAttribute("successMessage", "Successfully deleted the document.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting document: " + e.getMessage());
        }
        return "redirect:/profile";
    }
}

