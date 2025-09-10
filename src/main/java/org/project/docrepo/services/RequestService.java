package org.project.docrepo.services;

import org.project.docrepo.model.DownloadRequest;
import org.project.docrepo.model.User;
import org.project.docrepo.repo.DownloadRequestRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequestService {

    private final DownloadRequestRepo requestRepository;
    private final UserService userService;

    public RequestService(DownloadRequestRepo requestRepository, UserService userService) {
        this.requestRepository = requestRepository;
        this.userService = userService;
    }

    /**
     * Creates and saves a new download request after checking for duplicates.
     * This method contains the core business logic for the request process.
     *
     * @param downloadRequest The fully prepared DownloadRequest object from the controller.
     * @param student The student making the request.
     * @throws IllegalStateException if the student has already requested this document.
     */
    public void createRequest(DownloadRequest downloadRequest, User student) {
        // Business Rule: A student cannot request the same document more than once.
        // We use the custom query we defined in our DownloadRequestRepository.
        boolean alreadyExists = requestRepository.existsByDocumentIdAndStudentId(
                downloadRequest.getDocumentId(),
                student.getId()
        );

        if (alreadyExists) {
            // If the request exists, we throw an exception with a clear message.
            // The controller will catch this and show it to the user.
            throw new IllegalStateException("You have already submitted a request for this document.");
        }

        // If the check passes, we save the new request to the database.
        requestRepository.save(downloadRequest);
    }

    public List<DownloadRequest> findRequestsByStudentId(String id){
        return requestRepository.findAllByStudentId(id);
    }

    public List<DownloadRequest> findPendingRequestsByFacultyId(String id){
        return requestRepository.findAllByFacultyIdContainingAndStatus(id,"PENDING");
    }

    public List<DownloadRequest> findProcessedRequestsByFacultyId(String id){
        return requestRepository.findAllByFacultyIdContainingAndStatusNotContaining(id,"PENDING");
    }


    public void approveRequest(String id) throws Exception{

        DownloadRequest request = requestRepository.findById(id).orElseThrow(Exception::new);
        request.setStatus("APPROVED");
        User sender = userService.findUserById(request.getStudentId()).orElseThrow(Exception::new);
        sender.addAllowedDocument(request.getDocumentId());
        userService. save(sender);
        requestRepository.save(request);
    }

    public void denyRequest(String id) throws Exception{

        DownloadRequest request = requestRepository.findById(id).orElseThrow(Exception::new);
        request.setStatus("DENIED");
        requestRepository.save(request);
    }

    public void deleteRequestByDocId(String id) throws Exception{

        requestRepository.deleteDownloadRequestByDocumentIdContaining(id);
    }
}