package org.project.docrepo.controllers;

import org.project.docrepo.model.User;
import org.project.docrepo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;



@Controller
public class HomeController {

    @Autowired
    UserService userService;

    @GetMapping("/")
    public String showHomePage(@AuthenticationPrincipal User user, Model model){

        model.addAttribute("currentUser", user);
        return "home";
    }
}
