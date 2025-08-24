package org.project.docrepo.controllers;

import jakarta.validation.Valid;
import org.project.docrepo.model.DocumentDto;
import org.project.docrepo.model.Documents;
import org.project.docrepo.model.User;
import org.project.docrepo.repo.DocumentRepo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;


@Controller
public class DocumentController {

    private static final String UPLOAD_DIR = "./uploads/";
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
                                 RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "upload-form";
        }

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a file to upload!");
            return "redirect:/documents/upload";
        }

        try {
            System.out.println("--- [DEBUG] Starting file upload process ---");

            // Sanitize the original filename to remove invalid characters
            String originalFilename = file.getOriginalFilename();
            String safeFilename = (originalFilename == null) ? "file" : originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            String uniqueFileName = user.getDepartment() + "_" + user.getFullName() + "_" + safeFilename;

            Path uploadPath = Paths.get(UPLOAD_DIR);
            Path filePath = uploadPath.resolve(uniqueFileName);


            if (Files.notExists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Files.copy(file.getInputStream(), filePath);

            Documents document = new Documents();
            document.setTitle(documentDto.getTitle());
            document.setDescription(documentDto.getDescription());
            document.setTopic(documentDto.getTopic());
            document.setFacultyId(user.getId());
            document.setFilePath(filePath.toString());

            document.setUploadDate(LocalDate.now().toString());

            documentRepo.save(document);

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Could not upload the file due to a file system error.");
            return "redirect:/documents/upload";
        } catch (Exception e) { // Catch any other unexpected errors
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please check the logs.");
            return "redirect:/documents/upload";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Document uploaded successfully!");
        return "redirect:/";
    }
}