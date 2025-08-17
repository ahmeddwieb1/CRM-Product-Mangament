package org.elmorshedy.lead.service;

import org.bson.types.ObjectId;
import org.elmorshedy.lead.model.Lead;
import org.elmorshedy.lead.model.CreateLead;
import org.elmorshedy.lead.model.UpdateLead;

import java.util.List;

public interface LeadService {
    public Lead getLead(ObjectId id);

    public List<Lead> getLeads();

    Lead addLead(CreateLead leadRequest, String currentUsername);

    //لو user have lead
    List<Lead> getLeadsByUserid(ObjectId id);

    Lead updateLead(ObjectId id, UpdateLead leadRequest, String currentUsername);

    public void deleteLead(ObjectId id);
}
