package org.elmorshedy.lead.repo;

import org.bson.types.ObjectId;
import org.elmorshedy.lead.model.LeadDTO;

import java.util.List;

public interface LeadRepositoryCustom {
    List<LeadDTO> findAllWithUser();

    List<LeadDTO> findByAssignedToIdWithUser(ObjectId assignedToId);

    List<LeadDTO> findAllWithUserWithPage(int page, int size);
}
