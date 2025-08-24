package org.project.docrepo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Document")
public class Documents {

    @Id
    private String id;
    private String title, description, topic, filePath, uploadDate, facultyId;

    public Documents(String id, String title, String description, String topic, String filePath, String uploadDate, String facultyId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.topic = topic;
        this.filePath = filePath;
        this.uploadDate = uploadDate;
        this.facultyId = facultyId;
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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
}
