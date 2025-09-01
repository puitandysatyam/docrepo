package org.project.docrepo.model;

import jakarta.validation.constraints.NotBlank;

public class ProfileDTO {

    @NotBlank
    String fullName;

    @NotBlank
    String email;

    String profileDescription;

    @NotBlank
    String department;


    public ProfileDTO() {
    }

    public String getProfileDescription() {
        return profileDescription;
    }

    public void setProfileDescription(String profileDescription) {
        this.profileDescription = profileDescription;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
