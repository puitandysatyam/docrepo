package org.project.docrepo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Document")
public class Documents {

    @Id
    private String id;
    private String title, description, topic, driveFileId, uploadDate, facultyId, facultyName;

    public Documents(String id, String title, String description, String topic, String driveFileId, String uploadDate, String facultyId, String facultyName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.topic = topic;
        this.driveFileId = driveFileId;
        this.uploadDate = uploadDate;
        this.facultyId = facultyId;
        this.facultyName = facultyName;
    }

    public Documents() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDriveFileId() {
        return driveFileId;
    }

    public void setDriveFileId(String driveFileId) {
        this.driveFileId = driveFileId;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public String getFacultyName() { return facultyName; }

    public void setFacultyName( String facultyName ){ this.facultyName = facultyName; }
}
