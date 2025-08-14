package org.elmorshedy.lead.service;

import org.bson.types.ObjectId;
import org.elmorshedy.lead.model.Lead;

import java.util.List;

public interface LeadService {
    public Lead getLead(ObjectId id);

    public List<Lead> getLeads();

    public Lead addLead(Lead lead);

    public Lead updateLead(Lead lead);

    public void deleteLead(ObjectId id);
}
