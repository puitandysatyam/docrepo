package org.project.docrepo.services;

import org.project.docrepo.model.RegistrationDTO;
import org.project.docrepo.model.User;
import org.project.docrepo.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepo userRepo;
    @Autowired
    PasswordEncoder passwordEncoder;

    public User findUserByEmail(String email){
        return userRepo.findByEmail(email).orElse(null);
    }

    public void registerUser (RegistrationDTO registrationDTO){

        if(userRepo.existsByEmail(registrationDTO.getEmail())){
            throw new IllegalStateException();
        }
        else{
            User newUser = new User ();
            newUser.setFullName(registrationDTO.getFullName());
            newUser.setEmail(registrationDTO.getEmail());
            newUser.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
            newUser.setDepartment(registrationDTO.getDepartment());
            newUser.setRole(registrationDTO.getRole());
            newUser.setProfileImageUrl("https://placehold.co/400x400/EBF4FF/7F9CF5?text=Profile");

            userRepo.save(newUser);
        }
    }

    public Optional<User> findUserById(String id) {

        return userRepo.findById(id);
    }
}
