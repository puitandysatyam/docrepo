package org.project.docrepo.repo;

import org.project.docrepo.model.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepo extends MongoRepository<Department, String> {

}
