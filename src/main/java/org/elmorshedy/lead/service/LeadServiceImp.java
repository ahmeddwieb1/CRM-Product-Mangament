package org.elmorshedy.lead.service;

import org.bson.types.ObjectId;
import org.elmorshedy.lead.model.Lead;
import org.elmorshedy.lead.repo.LeadRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeadServiceImp implements LeadService {
    @Autowired
    private LeadRepo leadRepo;

    @Override
    public Lead getLead(ObjectId id) {
        return leadRepo.findById(id).orElse(null);
    }

    @Override
    public List<Lead> getLeads() {
        return leadRepo.findAll();
    }

    @Override
    public Lead addLead(Lead lead) {
        if (leadRepo.existsByPhone(lead.getPhone())) {
            //TODO :edit
            return null;
        }
        return leadRepo.save(lead);
    }

    @Override
    public Lead updateLead(Lead lead) {
        if (!leadRepo.existsByPhone(lead.getPhone())) {
            //TODO :edit
            return null;
        }
        return leadRepo.save(lead);
    }

    @Override
    public void deleteLead(ObjectId id) {
        leadRepo.deleteById(id);
    }
}
