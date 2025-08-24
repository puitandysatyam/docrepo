package org.project.docrepo.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationDTO {

    @NotBlank
    String fullName;

    @Size(min = 8, max = 32)
    String password;

    @Email
    String email;

    @NotBlank
    String department;

    String role;

    public RegistrationDTO(String fullName, String password, String email, String department, String role) {
        this.fullName = fullName;
        this.password = password;
        this.email = email;
        this.department = department;
        this.role = role;
    }

    public RegistrationDTO() {

    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
