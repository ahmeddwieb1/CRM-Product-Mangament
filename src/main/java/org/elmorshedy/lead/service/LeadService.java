package org.elmorshedy.lead.service;

import org.bson.types.ObjectId;
import org.elmorshedy.lead.model.Lead;
import org.elmorshedy.lead.model.RequestLead;
import org.elmorshedy.lead.model.LeadDTO;

import java.util.List;
import java.util.Optional;

public interface LeadService {

    Optional<LeadDTO> getLead(ObjectId id);

    List<LeadDTO> getAllLeads();

    LeadDTO addLead(RequestLead leadRequest, String currentUsername);


    LeadDTO updateLead(ObjectId id, RequestLead leadRequest, String currentUsername);

    void deleteLead(ObjectId id);

    LeadDTO addNoteToLead(ObjectId meetingId, String noteContent);

    LeadDTO deleteNoteFromLead(ObjectId id, String content);
}
