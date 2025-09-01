package org.project.docrepo.controllers;


import jakarta.validation.Valid;
import org.project.docrepo.model.Documents;
import org.project.docrepo.model.ProfileDTO;
import org.project.docrepo.model.User;
import org.project.docrepo.services.DocumentService;
import org.project.docrepo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ProfileController {

    @Autowired
    UserService userService;
    @Autowired
    private DocumentService documentService;

    @GetMapping("/profile")
    public String viewProfile(Model model, @AuthenticationPrincipal User faculty){

        model.addAttribute("faculty", faculty);
        List<Documents> documents = documentService.findDocByFacultyId(faculty.getId());
        model.addAttribute("documents",documents);

        return "profile";

    }

    @GetMapping("/profile/edit")
    public String showEditProfileForm(Model model, @AuthenticationPrincipal User faculty){
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setFullName(faculty.getFullName());
        profileDTO.setDepartment(faculty.getDepartment());
        profileDTO.setProfileDescription(faculty.getProfileDescription());
        profileDTO.setEmail(faculty.getEmail());
        profileDTO.setProfileImageUrl(faculty.getProfileImageUrl());

        model.addAttribute("profileDto", profileDTO);

        return "edit-profile";
    }

    @PostMapping("/profile/update")
    public String editProfile(@Valid @ModelAttribute("profileDto") ProfileDTO profileDTO,
                              Model model,
                              @AuthenticationPrincipal User existingProfile,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes){

        if(bindingResult.hasErrors()){
            return "edit-profile";
        }
        try {
            userService.updateUser(existingProfile, profileDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Profile Update Successfully. ");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage","Failed to update profile. Try again later !");
            return "redirect:/profile";
        }


    }
}
