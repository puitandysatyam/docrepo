package org.project.docrepo.services;

import org.project.docrepo.model.Document;
import org.project.docrepo.repo.DocumentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {

    @Autowired
    DocumentRepo documentRepo;

    public List<Document> findDocById(String id){

        return documentRepo.findByFacultyId(id);
    }
}
