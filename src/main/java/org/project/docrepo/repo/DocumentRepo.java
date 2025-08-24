package org.project.docrepo.repo;

import org.project.docrepo.model.Documents;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepo extends MongoRepository<Documents, String> {
    List<Documents> findByTitle(String title);
    List<Documents> findByFacultyId(String facultyId);
    List<Documents> findByTopic(String topic);

    @Query("{'$or':[ {'title': {'$regex': ?0, '$options': 'i'}}, {'topic': {'$regex': ?0, '$options': 'i'}}, {'facultyName': {'$regex': ?0, '$options': 'i'}} ]}")
    List<Documents> searchByTopicTitleOrFaculty(String query);


}
