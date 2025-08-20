package org.project.docrepo.repo;

import org.project.docrepo.model.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DocumentRepo extends MongoRepository<Document, String> {
    List<Document> findByTitle(String title);
    List<Document> findByFacultyId(String facultyId);
    List<Document> findByTopic(String topic);


}
