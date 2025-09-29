package org.elmorshedy.lead.repo;

import org.bson.types.ObjectId;
import org.elmorshedy.lead.model.LeadDTO;

import java.util.List;
import java.util.Optional;

public interface LeadRepositoryCustom {
    List<LeadDTO> findAllWithUser();

    List<LeadDTO> findByAssignedToIdWithUser(ObjectId assignedToId);

    Optional<LeadDTO> findByIdWithUser(ObjectId Id);

    List<LeadDTO> findAllWithUserWithPage(int page, int size);
}
