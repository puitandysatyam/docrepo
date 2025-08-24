package org.project.docrepo.controllers;

import org.project.docrepo.model.RegistrationDTO;
import org.project.docrepo.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(){

        return "login";
    }
    @GetMapping("/register/student")
    public String showStudentRegistrationForm(Model model){
        RegistrationDTO newUserDTO = new RegistrationDTO();
        newUserDTO.setRole("STUDENT");
        model.addAttribute("userDto", newUserDTO);
        model.addAttribute("pageTitle", "Student Registration");
        return "register";

    }

    @GetMapping("/register/faculty")
    public String showFacultyRegistrationForm(Model model){
        RegistrationDTO newUserDTO = new RegistrationDTO();
        newUserDTO.setRole("FACULTY");
        model.addAttribute("userDto", newUserDTO);
        model.addAttribute("pageTitle", "Faculty Registration");
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userDTO") RegistrationDTO registrationDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes){

        if(bindingResult.hasErrors()){
            model.addAttribute("pageTitle", "FACULTY".equals(registrationDTO.getRole())? "Faculty Registration": "Student Registration");
            return "register";
        }

        try{
            userService.registerUser(registrationDTO);
        }
        catch(IllegalStateException e){
            model.addAttribute("pageTitle", "FACULTY".equals(registrationDTO.getRole())? "Faculty Registration": "Student Registration");
            bindingResult.rejectValue("email", "email.exists", e.getMessage());
            return "register";
        }
        redirectAttributes.addFlashAttribute("successMessage","Account created. Please proceed to login .");
        return "redirect:/login";

    }

}
