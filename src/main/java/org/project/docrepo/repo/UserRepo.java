package org.project.docrepo.repo;


import org.project.docrepo.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepo extends MongoRepository<User, String> {

    String findEmailById(String Id);
    List<User> findByDepartment(String department);

}
