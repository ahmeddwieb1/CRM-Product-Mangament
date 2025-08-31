package org.elmorshedy.note.Repo;

import org.bson.types.ObjectId;
import org.elmorshedy.note.models.Phone;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PhoneRepo extends MongoRepository<Phone, ObjectId> {
    Optional<Phone> findByPhone(String phone);
}
