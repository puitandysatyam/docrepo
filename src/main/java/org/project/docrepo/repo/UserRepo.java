package org.project.docrepo.repo;


import org.project.docrepo.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends MongoRepository<User, String> {

    String findEmailById(String Id);
    List<User> findByDepartment(String department);
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByDepartmentContainingIgnoreCaseAndRoleContaining(String department, String role);

}
