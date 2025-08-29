package org.project.docrepo.controllers;

import jakarta.validation.Valid;
import org.project.docrepo.model.DocumentDto;
import org.project.docrepo.model.Documents;
import org.project.docrepo.model.User;
import org.project.docrepo.repo.DocumentRepo;
import org.project.docrepo.services.GoogleDriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.List;


@Controller
public class DocumentController {

    @Autowired
    GoogleDriveService googleDriveService;

    private final DocumentRepo documentRepo;

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
}