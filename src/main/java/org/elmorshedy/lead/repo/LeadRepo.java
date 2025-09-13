package org.elmorshedy.lead.repo;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.bson.types.ObjectId;
import org.elmorshedy.lead.model.Lead;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LeadRepo extends MongoRepository<Lead, ObjectId> {

    boolean existsByPhone(@Size(min = 10, max = 12)
                          @Pattern(regexp = "^[0-9]+$",
                                  message = "Phone number must contain only digits")
                          String phone);

    Optional<Lead> findByPhone(String phone);

    List<Lead> findByAssignedToId(ObjectId assignedToId);
}
