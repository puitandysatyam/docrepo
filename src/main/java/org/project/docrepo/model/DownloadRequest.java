package org.project.docrepo.model;

import org.springframework.data.annotation.Id;

public class DownloadRequest {

    @Id
    private String id;
    private String documentId, studentId, facultyId, requestDate, status;

    public DownloadRequest(String id, String documentId, String studentId, String facultyId, String requestDate, String status) {
        this.id = id;
        this.documentId = documentId;
        this.studentId = studentId;
        this.facultyId = facultyId;
        this.requestDate = requestDate;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
