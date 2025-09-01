package org.project.docrepo.controllers;

import jakarta.validation.Valid;
import org.project.docrepo.model.Documents;
import org.project.docrepo.model.ProfileDTO;
import org.project.docrepo.model.User;
import org.project.docrepo.services.CloudinaryStorageService;
import org.project.docrepo.services.DocumentService;
import org.project.docrepo.services.UserService;
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

import java.util.List;

@Controller
public class ProfileController {

    private final UserService userService;
    private final DocumentService documentService;
    private final CloudinaryStorageService cloudinaryStorageService;

    public ProfileController(UserService userService, DocumentService documentService, CloudinaryStorageService cloudinaryStorageService) {
        this.userService = userService;
        this.documentService = documentService;
        this.cloudinaryStorageService = cloudinaryStorageService;
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, @AuthenticationPrincipal User faculty){
        model.addAttribute("faculty", faculty);
        List<Documents> documents = documentService.findDocByFacultyId(faculty.getId());
        model.addAttribute("documents", documents);
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String showEditProfileForm(Model model, @AuthenticationPrincipal User faculty){
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setFullName(faculty.getFullName());
        profileDTO.setDepartment(faculty.getDepartment());
        profileDTO.setProfileDescription(faculty.getProfileDescription());
        profileDTO.setEmail(faculty.getEmail());
        model.addAttribute("profileDto", profileDTO);
        return "edit-profile";
    }

    @PostMapping("/profile/update")
    public String editProfile(@Valid @ModelAttribute("profileDto") ProfileDTO profileDTO,
                              BindingResult bindingResult,
                              @RequestParam("profileImageFile") MultipartFile profileImageFile,
                              @AuthenticationPrincipal User existingProfile,
                              RedirectAttributes redirectAttributes,
                              Model model) {

        if(bindingResult.hasErrors()){
            return "edit-profile";
        }

        try {
            String newImageUrl = null;
            if (!profileImageFile.isEmpty()) {
                newImageUrl = cloudinaryStorageService.uploadProfileImage(profileImageFile, existingProfile.getId());
            }

            userService.updateUser(existingProfile.getId(), profileDTO, newImageUrl);

            redirectAttributes.addFlashAttribute("successMessage", "Profile Updated! Please log out and log back in to see all changes.");
            return "redirect:/profile";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to update profile: " + e.getMessage());
            model.addAttribute("profileDto", profileDTO);
            return "edit-profile";
        }
    }
}
