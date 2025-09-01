package org.project.docrepo.services;

import org.project.docrepo.model.ProfileDTO;
import org.project.docrepo.model.RegistrationDTO;
import org.project.docrepo.model.User;
import org.project.docrepo.repo.UserRepo;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * This service class contains all business logic related to User operations,
 * such as registration, finding users, and updating profiles.
 */
@Service
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public User findUserByEmail(String email){
        return userRepo.findByEmail(email).orElse(null);
    }

    public void registerUser (RegistrationDTO registrationDTO){
        // Prevent duplicate registrations by checking if the email already exists.
        if(userRepo.existsByEmail(registrationDTO.getEmail())){
            throw new IllegalStateException("User with this email already exists.");
        }
        User newUser = new User ();
        newUser.setFullName(registrationDTO.getFullName());
        newUser.setEmail(registrationDTO.getEmail());
        newUser.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        newUser.setDepartment(registrationDTO.getDepartment());
        newUser.setRole(registrationDTO.getRole().trim());
        newUser.setProfileImageUrl("https://placehold.co/400x400/EBF4FF/7F9CF5?text=Profile");
        newUser.setProfileDescription("");
        userRepo.save(newUser);
    }

    public Optional<User> findUserById(String id) {
        return userRepo.findById(id);
    }

    public List<User> findFacultyByDept(String deptName){
        return userRepo.findByDepartmentContainingIgnoreCaseAndRoleContaining(deptName,"FACULTY");
    }


    public void updateUser(String userId, ProfileDTO profileDTO, String newImageUrl){
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        user.setFullName(profileDTO.getFullName());
        user.setProfileDescription(profileDTO.getProfileDescription());
        user.setDepartment(profileDTO.getDepartment());
        user.setEmail(profileDTO.getEmail());

        if (newImageUrl != null && !newImageUrl.isBlank()) {
            user.setProfileImageUrl(newImageUrl);
        }

        userRepo.save(user);
    }

    public void save(User user) {
        userRepo.save(user);
    }
}