package org.project.docrepo.controllers;

import org.project.docrepo.model.Department;
import org.project.docrepo.model.Documents;
import org.project.docrepo.model.User;
import org.project.docrepo.services.DepartmentService;
import org.project.docrepo.services.DocumentService;
import org.project.docrepo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class BrowseController {

    @Autowired
    DepartmentService departmentService;
    @Autowired
    private UserService userService;
    @Autowired
    private DocumentService documentService;

    @GetMapping("/departments")
    public String showDepartments(Model model){

        List<Department> departments = departmentService.showDepartments();
        model.addAttribute("departments", departments);

        return "departments";

    }

    @GetMapping("/departments/{deptName}/faculty")
    public String showFaculty(Model model, @PathVariable String deptName){

        List<User> faculty = userService.findFacultyByDept(deptName);
        model.addAttribute("facultyMembers", faculty);
        model.addAttribute("departmentName",deptName);

        return "faculty-list";

    }

    @GetMapping("/browse/{deptName}/{facultyId}")
    public String showFacultyDetails(Model model , @PathVariable String facultyId){

        User faculty = userService.findUserById(facultyId).orElse(null);
        model.addAttribute("faculty", faculty);
        List<Documents> documents = documentService.findDocByFacultyId(facultyId);
        model.addAttribute("documents", documents);
        return "faculty-details";
    }
}
