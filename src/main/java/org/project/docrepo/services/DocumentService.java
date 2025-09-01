package org.project.docrepo.services;

import org.project.docrepo.model.Documents;
import org.project.docrepo.repo.DocumentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {

    @Autowired
    DocumentRepo documentRepo;

    public List<Documents> findDocByFacultyId(String id){

        return documentRepo.findByFacultyId(id);
    }
}
