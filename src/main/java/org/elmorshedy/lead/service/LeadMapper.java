package org.elmorshedy.lead.service;

import org.elmorshedy.lead.model.Lead;
import org.elmorshedy.lead.model.LeadDTO;
import org.elmorshedy.user.model.User;
import org.elmorshedy.user.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LeadMapper {

    private final UserRepo userRepo;

    @Autowired
    public LeadMapper(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public LeadDTO toDTO(Lead lead) {
        LeadDTO dto = new LeadDTO();
        dto.setId(lead.getId() != null ? lead.getId().toHexString() : null);
        dto.setLeadName(lead.getLeadName());
        dto.setPhone(lead.getPhone());
        dto.setBudget(lead.getBudget());
        dto.setLeadSource(lead.getLeadSource());
        dto.setLeadStatus(lead.getLeadStatus());
        dto.setNotes(lead.getNotes());

        // Get assigned user name
        if (lead.getAssignedToId() != null) {
            User user = userRepo.findById(lead.getAssignedToId()).orElse(null);
            dto.setAssignedToName(user != null ? user.getUsername() : "Unknown User");
        } else {
            dto.setAssignedToName("Not Assigned");
        }

        return dto;
    }
}