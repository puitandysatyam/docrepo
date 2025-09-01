package org.project.docrepo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList; // <-- Import this
import java.util.Collection;
import java.util.List;

@Document(collection = "User")
public class User implements UserDetails {

    @Id
    String id;

    String fullName;
    String email;
    String password;
    String role;
    String department;
    String profileImageUrl;
    String profileDescription;
    private List<String> allowedDocumentsId = new ArrayList<>();

    public User() {
    }

    public List<String> getAllowedDocumentsId() {
        return allowedDocumentsId;
    }

    public void setAllowedDocumentsId(List<String> allowedDocumentsId) {
        this.allowedDocumentsId = allowedDocumentsId;
    }

    public void addAllowedDocument(String documentId) {
        if (this.allowedDocumentsId == null) {
            this.allowedDocumentsId = new ArrayList<>();
        }
        if (!this.allowedDocumentsId.contains(documentId)) {
            this.allowedDocumentsId.add(documentId);
        }
    }

    public String getProfileDescription() {
        return profileDescription;
    }

    public void setProfileDescription(String profileDescription) {
        this.profileDescription = profileDescription;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
