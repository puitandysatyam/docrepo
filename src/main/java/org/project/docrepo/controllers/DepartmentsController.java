package org.project.docrepo.controllers;

import org.project.docrepo.model.Department;
import org.project.docrepo.services.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DepartmentsController {

    @Autowired
    DepartmentService departmentService;

    @GetMapping("/departments")
    public String showDepartments(Model model){

        List<Department> departments = departmentService.showDepartments();
        model.addAttribute("departments", departments);

        return "departments";

    }
}
