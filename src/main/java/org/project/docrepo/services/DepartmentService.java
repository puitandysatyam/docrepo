package org.project.docrepo.services;

import org.project.docrepo.model.Department;
import org.project.docrepo.repo.DepartmentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DepartmentService {

    @Autowired
    DepartmentRepo departmentRepo;

    public List<Department> showDepartments(){

        return departmentRepo.findAll();
    }
}
