package org.project.docrepo.repo;


import org.project.docrepo.model.DownloadRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DownloadRequestRepo extends MongoRepository<DownloadRequest,String> {
    boolean existsByDocumentIdAndStudentId(String documentId, String id);

    List<DownloadRequest> findAllByStudentId(String id);

    List<DownloadRequest> findAllByFacultyIdContainingAndStatus(String facultyId, String status);

    List<DownloadRequest> findAllByFacultyIdContainingAndStatusNotContaining(String facultyId, String status);

    void deleteDownloadRequestByDocumentIdContaining(String id);
}
