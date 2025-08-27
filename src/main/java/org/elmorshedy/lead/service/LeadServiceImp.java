package org.elmorshedy.lead.service;

import org.bson.types.ObjectId;
import org.elmorshedy.lead.model.Lead;
import org.elmorshedy.lead.model.CreateLead;
import org.elmorshedy.lead.model.LeadDTO;
import org.elmorshedy.lead.model.UpdateLead;
import org.elmorshedy.lead.repo.LeadRepo;
import org.elmorshedy.user.model.AppRole;
import org.elmorshedy.user.model.User;
import org.elmorshedy.user.repo.UserRepo;
import org.elmorshedy.user.service.UserServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeadServiceImp implements LeadService {
    @Autowired
    private UserRepo userRepo;

    private final LeadRepo leadRepo;
    private final UserServiceImp userService;

    @Autowired
    public LeadServiceImp(LeadRepo leadRepo, UserServiceImp userService) {
        this.leadRepo = leadRepo;
        this.userService = userService;
    }
//done
 @Override
public Optional<LeadDTO> getLead(ObjectId id) {
    return leadRepo.findById(id)
            .map(lead -> new LeadDTO(
                    lead.getId().toHexString(),
                    lead.getLeadName(),
                    lead.getPhone(),
                    lead.getBudget(),
                    lead.getLeadSource() != null ? lead.getLeadSource().toString() : null,
                    lead.getLeadStatus() != null ? lead.getLeadStatus().toString() : null,
                    lead.getAssignedTo() != null ? lead.getAssignedTo().getUsername() : null

//                    ,lead.getNotes().stream()
//                            .map(Note::getContent)
//                            .collect(Collectors.toList()
                            ));
}

    @Override
    public List<LeadDTO> getAllLeads() {
        return leadRepo.findAll().stream()
                .map(lead -> new LeadDTO(
                        lead.getId().toHexString(),
                        lead.getLeadName(),
                        lead.getPhone(),
                        lead.getBudget(),
                        lead.getLeadSource() != null ? lead.getLeadSource().toString() : null,
                        lead.getLeadStatus() != null ? lead.getLeadStatus().toString() : null,
                        lead.getAssignedTo() != null ? lead.getAssignedTo().getUsername() : null
//                        ,lead.getNotes().stream()
//                                .map(Note::getContent)
//                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Lead addLead(CreateLead leadRequest, String currentUsername) {
        if (leadRepo.existsByPhone(leadRequest.getPhone())) {
            throw new IllegalArgumentException("Lead with this phone already exists");
        }

        User currentUser = userService.findbyusername(currentUsername);
        Lead lead = new Lead();
        lead.setLeadName(leadRequest.getLeadName());
        lead.setPhone(leadRequest.getPhone());
        lead.setBudget(leadRequest.getBudget());
        lead.setLeadStatus(leadRequest.getLeadStatus());
        lead.setLeadSource(leadRequest.getLeadSource());

        // إذا كان admin ويحدد assignedTo
        if (isAdmin(currentUser) && leadRequest.getAssignedTo() != null) {
            User assignedTo = userService.findbyusername(leadRequest.getAssignedTo());
            lead.setAssignedTo(assignedTo);

            // ضيف الـ lead لليوزر
//            assignedTo.getLeads().add(lead);
            userRepo.save(assignedTo);
        } else {
            lead.setAssignedTo(currentUser);
//            currentUser.getLeads().add(lead);
            userRepo.save(currentUser);
        }

        return leadRepo.save(lead);
    }
    //لو user have lead
    @Override
    public List<Lead> getLeadsByUserid(ObjectId id) {
        return leadRepo.findByAssignedTo_Id(id);
    }


    @Override
    public Lead updateLead(ObjectId id, UpdateLead leadRequest, String currentUsername) {
        User currentUser = userService.findbyusername(currentUsername);
//       todo : make check all case are true
        Lead lead = leadRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        if (isAdmin(currentUser) && leadRequest.getAssignedTo() != null) {
            User assignedTo = userService.findbyusername(leadRequest.getAssignedTo());
            lead.setAssignedTo(assignedTo);
        }

        if (leadRequest.getLeadStatus() != null) {
            lead.setLeadStatus(leadRequest.getLeadStatus());
        }

        return leadRepo.save(lead);
    }

    @Override
    public void deleteLead(ObjectId id) {
        if (!leadRepo.existsById(id)) {
            throw new RuntimeException("Lead not found with id: " + id);
        }
        leadRepo.deleteById(id);
    }

    private boolean isAdmin(User user) {
        return user.getRole().getRolename().equals(AppRole.ROLE_ADMIN);
    }
}
