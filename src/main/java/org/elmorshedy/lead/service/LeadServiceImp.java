package org.elmorshedy.lead.service;

import org.bson.types.ObjectId;
import org.elmorshedy.lead.model.Lead;
import org.elmorshedy.lead.model.RequestLead;
import org.elmorshedy.lead.model.LeadDTO;
import org.elmorshedy.lead.repo.LeadRepo;
import org.elmorshedy.meeting.service.MeetingServiceImp;
import org.elmorshedy.user.model.AppRole;
import org.elmorshedy.user.model.User;
import org.elmorshedy.user.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeadServiceImp implements LeadService {


    private final LeadRepo leadRepo;
    private final UserRepo userRepo;
    private final LeadMapper leadMapper;
    private final MeetingServiceImp meetingService;

    @Autowired
    public LeadServiceImp(LeadRepo leadRepo,
                          UserRepo userRepo,
                          LeadMapper leadMapper,
                          MeetingServiceImp meetingService) {
        this.leadRepo = leadRepo;
        this.userRepo = userRepo;
        this.leadMapper = leadMapper;
        this.meetingService = meetingService;
    }

    //done
    @Override
    public Optional<LeadDTO> getLead(ObjectId id) {
        return leadRepo.findById(id)
                .map(leadMapper::toDTO);
    }
    @Override
    public List<LeadDTO> getLeadsForSales(ObjectId assignedToId) {
        return leadRepo.findByAssignedToId(assignedToId)
                .stream()
                .map(leadMapper::toDTO)
                .toList();
    }



    @Override
    public List<LeadDTO> getAllLeads() {
        return leadRepo.findAll().stream()
                .map(leadMapper::toDTO)
                .collect(Collectors.toList());
    }

    public LeadDTO addLead(RequestLead leadRequest, String currentUsername) {
        validateLeadRequest(leadRequest);

        User currentUser = userRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NoSuchElementException("Current user not found"));

        Lead lead = createLeadFromRequest(leadRequest);

        assignLeadToUser(lead, currentUser, leadRequest);

        Lead savedLead = leadRepo.save(lead);
        return leadMapper.toDTO(savedLead);
    }

    private Lead createLeadFromRequest(RequestLead request) {
        Lead lead = new Lead();
        lead.setLeadName(request.getLeadName());
        lead.setPhone(request.getPhone());
        lead.setBudget(request.getBudget());
        lead.setLeadStatus(request.getLeadStatus());
        lead.setLeadSource(request.getLeadSource());
        if (request.getNotes() != null)
            lead.setNotes(request.getNotes());
        return lead;
    }

    private void assignLeadToUser(Lead lead, User currentUser, RequestLead request) {
        if (isAdmin(currentUser) && request.getAssignedToId() != null) {
            User assignedTo = userRepo.findById(new ObjectId(request.getAssignedToId()))
                    .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + request.getAssignedToId()));
            lead.setAssignedToId(assignedTo.getId());
        } else {
            lead.setAssignedToId(currentUser.getId());
        }
    }

    private void validateLeadRequest(RequestLead request) {
        if (leadRepo.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Lead with this phone already exists");
        }

        if (request.getAssignedToId() != null && !request.getAssignedToId().isEmpty()) {
            try {
                ObjectId assignedToObjectId = new ObjectId(request.getAssignedToId());

                if (!userRepo.existsById(assignedToObjectId)) {
                    throw new NoSuchElementException("User with id " + request.getAssignedToId() + " not found");
                }

            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid user ID format: " + request.getAssignedToId());
            }
        }
    }
//لو user have lead
//    @Override
//    public List<Lead> getLeadsByUserid(ObjectId id) {
//        return leadRepo.findByAssignedTo_Id(id);
//    }

    @Override
    public LeadDTO updateLead(ObjectId id, RequestLead leadRequest, String currentUsername) {
        User currentUser = userRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NoSuchElementException("Current user not found"));
        Lead lead = leadRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Lead not found"));

        Lead updatelead = updateLeadFromRequest(leadRequest, lead);

        assignLeadToUser(updatelead, currentUser, leadRequest);

        Lead savedLead = leadRepo.save(updatelead);
        return leadMapper.toDTO(savedLead);
    }

    public LeadDTO updateLeadStatus(ObjectId id, RequestLead leadRequest) {
        Lead lead = leadRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Lead not found"));

        if (leadRequest.getLeadStatus() != null) {
            lead.setLeadStatus(leadRequest.getLeadStatus());
        }

        Lead savedLead = leadRepo.save(lead);
        return leadMapper.toDTO(savedLead);
    }

    @Override
    public void deleteLead(ObjectId id) {
        if (!leadRepo.existsById(id)) {
            throw new IllegalArgumentException("Lead not found with id: " + id);
        }
        meetingService.deleteMeetingsByLeadId(id);
        leadRepo.deleteById(id);
    }

    @Override
    public LeadDTO addNoteToLead(ObjectId leadId, String noteContent){
        Lead lead = leadRepo.findById(leadId)
                .orElseThrow(() -> new NoSuchElementException("Lead not found"));

        if (lead.getNotes() == null) {
            lead.setNotes(new ArrayList<>());
        }

        lead.getNotes().add(noteContent);

        Lead updated = leadRepo.save(lead);
        return leadMapper.toDTO(updated);
    }

    @Override
    public LeadDTO deleteNoteFromLead(ObjectId id, String content) {
        Lead lead = leadRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Lead not found with ID: " + id));
        if (lead.getNotes() == null || lead.getNotes().isEmpty()) {
            throw new IllegalArgumentException("Lead has no notes to delete");
        }
        boolean remove = lead.getNotes().removeIf(note -> note.equals(content));

        if (!remove) {
            throw new NoSuchElementException("Note not found in Lead");
        }
        Lead updated = leadRepo.save(lead);
        return leadMapper.toDTO(updated);
    }

    private Lead updateLeadFromRequest(RequestLead requestLead, Lead lead) {
        if (requestLead.getLeadName() != null) {
            lead.setLeadName(requestLead.getLeadName());
        }
        if (requestLead.getLeadStatus() != null) {
            lead.setLeadStatus(requestLead.getLeadStatus());
        }
        if (requestLead.getLeadSource() != null) {
            lead.setLeadSource(requestLead.getLeadSource());
        }
        if (requestLead.getNotes() != null) {
            lead.setNotes(requestLead.getNotes());
        }
        if (requestLead.getBudget() > 0) {
            lead.setBudget(requestLead.getBudget());
        }
        if (requestLead.getPhone() != null && !requestLead.getPhone().trim().isEmpty()) {
            String newPhone = requestLead.getPhone().trim();

            if (!newPhone.matches("^[0-9]+$")) {
                throw new IllegalArgumentException("Phone number must contain only digits");
            }

            if (newPhone.length() < 10 || newPhone.length() > 12) {
                throw new IllegalArgumentException("Phone number must be between 10 and 12 digits");
            }

            if (!newPhone.equals(lead.getPhone())) {
                Optional<Lead> existingLead = leadRepo.findByPhone(newPhone);
                if (existingLead.isPresent() && !existingLead.get().getId().equals(lead.getId())) {
                    throw new IllegalArgumentException("Phone number already exists");
                }
                lead.setPhone(newPhone);
            }
        }
        return lead;
    }



    private boolean isAdmin(User user) {
        return user.getRole().getRolename().equals(AppRole.ROLE_ADMIN);
    }


}
