package org.project.docrepo.controllers;


import org.project.docrepo.model.Documents;
import org.project.docrepo.model.User;
import org.project.docrepo.services.DocumentService;
import org.project.docrepo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class ProfileController {

    @Autowired
    UserService userService;
    @Autowired
    private DocumentService documentService;

    @GetMapping("/profile/{id}")
    public String viewProfile(Model model, @PathVariable String id){

        User faculty = userService.findUserById(id).orElse(null);
        model.addAttribute("faculty", faculty);
        List<Documents> documents = documentService.findDocById(id);
        model.addAttribute("documents",documents);

        return "profile";

    }
}
