package org.elmorshedy.user.repo;

import org.bson.types.ObjectId;
import org.elmorshedy.user.model.AppRole;
import org.elmorshedy.user.model.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoleRepo extends MongoRepository<Role, ObjectId> {
    Optional<Role> findByRolename(AppRole appRole);

}
