package org.elmorshedy.lead.repo;

import org.elmorshedy.lead.model.LeadDTO;

import java.util.List;

public interface LeadRepositoryCustom {
    List<LeadDTO> findAllWithUser();

    List<LeadDTO> findAllWithUserWithPage(int page, int size);
}
