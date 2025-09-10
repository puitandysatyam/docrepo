package org.project.docrepo.controllers;

import org.project.docrepo.model.Department;
import org.project.docrepo.model.RegistrationDTO;
import org.project.docrepo.services.DepartmentService;
import org.project.docrepo.services.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.util.List;

@Controller
public class AuthController {

    private final UserService userService;
    private final DepartmentService departmentService;

    public AuthController(UserService userService, DepartmentService departmentService) {
        this.userService = userService;
        this.departmentService = departmentService;
    }

    @GetMapping("/login")
    public String loginPage(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated()) {

            return "redirect:/";
        }

        return "login";
    }
    @GetMapping("/register/student")
    public String showStudentRegistrationForm(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated()) {

            return "redirect:/";
        }
        RegistrationDTO newUserDTO = new RegistrationDTO();
        newUserDTO.setRole("STUDENT");
        List<Department> departments = departmentService.showDepartments();
        model.addAttribute("userDto", newUserDTO);
        model.addAttribute("pageTitle", "Student Registration");
        model.addAttribute("departments", departments);
        return "register";

    }

    @GetMapping("/register/faculty")
    public String showFacultyRegistrationForm(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated()) {

            return "redirect:/";
        }
        RegistrationDTO newUserDTO = new RegistrationDTO();
        newUserDTO.setRole("FACULTY");
        List<Department> departments = departmentService.showDepartments();
        model.addAttribute("userDto", newUserDTO);
        model.addAttribute("pageTitle", "Faculty Registration");
        model.addAttribute("departments", departments);
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
