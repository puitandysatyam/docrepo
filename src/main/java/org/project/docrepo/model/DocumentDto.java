package org.project.docrepo.model;

import jakarta.validation.constraints.NotBlank;

public class DocumentDto {

    @NotBlank
    String title;
    @NotBlank
    String description;
    @NotBlank
    String topic;


    public DocumentDto() {
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
}
