package org.project.docrepo.controllers;

import org.project.docrepo.model.Documents;
import org.project.docrepo.model.DownloadRequest;
import org.project.docrepo.model.User;
import org.project.docrepo.repo.DocumentRepo;
import org.project.docrepo.services.RequestService; // Assuming you have this service
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class RequestController {

    private final DocumentRepo documentRepo;
    private final RequestService requestService; // Renamed for clarity

    public RequestController(DocumentRepo documentRepo, RequestService requestService) {
        this.documentRepo = documentRepo;
        this.requestService = requestService;
    }

    @PostMapping("/documents/{docId}/request")
    public String createRequest(@RequestParam(required = false, defaultValue = "") String reason, // Reason is optional
                                @PathVariable String docId,
                                @AuthenticationPrincipal User student,
                                RedirectAttributes redirectAttributes) {

        try {
            // FIX 1: Safely fetch the document or throw an error
            Documents doc = documentRepo.findById(docId)
                    .orElseThrow(() -> new IllegalStateException("Document not found with ID: " + docId));

            DownloadRequest downloadRequest = new DownloadRequest();
            downloadRequest.setDocumentId(docId);
            downloadRequest.setDocumentTitle(doc.getTitle()); // It's good practice to store the title
            downloadRequest.setStudentName(student.getFullName());
            downloadRequest.setStudentId(student.getId());
            downloadRequest.setFacultyId(doc.getFacultyId());
            downloadRequest.setReason(reason);

            // FIX 2: Set the correct data types for Date and Status
            downloadRequest.setRequestDate(LocalDate.now().toString());
            downloadRequest.setStatus("PENDING");

            // Let the service handle the creation logic (like checking for duplicates)
            requestService.createRequest(downloadRequest, student);

            redirectAttributes.addFlashAttribute("successMessage", "Your request was sent successfully!");

        } catch (IllegalStateException e) {
            // Catch any errors (like "document not found" or "request already exists")
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        // FIX 3: Redirect to a valid, known URL. The homepage is a safe choice.
        return "redirect:/";
    }

    @GetMapping("/requests/my-requests")
    public String showMyRequests(Model model, @AuthenticationPrincipal User user){

        List<DownloadRequest> requests = requestService.findRequestsByStudentId(user.getId());

        model.addAttribute("requests", requests);

        return "my-requests";
    }

    @GetMapping("/requests/review")
    public String showPendingRequests(Model model, @AuthenticationPrincipal User user){

        List<DownloadRequest> pendingRequests = requestService.findPendingRequestsByFacultyId(user.getId());
        List<DownloadRequest> processedRequests = requestService.findProcessedRequestsByFacultyId(user.getId());

        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("processedRequests", processedRequests);
        return "review-requests";
    }

    @PostMapping("/requests/{id}/approve")
    public  String approveRequests(@PathVariable String id, RedirectAttributes redirectAttributes){
        try{
            requestService.approveRequest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Approved the Request");
            return "redirect:/requests/review";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error Occurred Approving the request. Try Again!!");
            return "redirect:/requests/review";
        }

    }

    @PostMapping("/requests/{id}/deny")
    public  String denyRequests(@PathVariable String id, RedirectAttributes redirectAttributes){
        try{
            requestService.denyRequest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Rejected the Request");
            return "redirect:/requests/review";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error Occurred Rejecting the request. Try Again!!");
            return "redirect:/requests/review";
        }

    }
}
