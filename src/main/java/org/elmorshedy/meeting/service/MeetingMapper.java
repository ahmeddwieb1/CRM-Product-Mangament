package org.elmorshedy.meeting.service;

import org.bson.types.ObjectId;
import org.elmorshedy.lead.model.Lead;
import org.elmorshedy.lead.repo.LeadRepo;
import org.elmorshedy.meeting.model.Meeting;
import org.elmorshedy.meeting.model.MeetingDTO;
import org.elmorshedy.user.model.User;
import org.elmorshedy.user.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class MeetingMapper {

    private final UserRepo userRepo;
    private final LeadRepo leadRepo;

    @Autowired
    public MeetingMapper(UserRepo userRepo, LeadRepo leadRepo) {
        this.userRepo = userRepo;
        this.leadRepo = leadRepo;
    }

    public MeetingDTO toDTO(Meeting meeting) {
        MeetingDTO dto = new MeetingDTO();

        dto.setId(meeting.getId().toHexString());
        dto.setTitle(meeting.getTitle());

        // Resolve client name
        Lead lead = leadRepo.findById(new ObjectId(meeting.getClientid())).orElse(null);
        dto.setClientName(lead != null ? lead.getLeadName() : "Unknown Lead");

        dto.setDate(meeting.getDate());
        dto.setTime(meeting.getTime());
        dto.setDuration(meeting.getDuration());
        dto.setType(meeting.getType());
        dto.setStatus(meeting.getStatus());
        dto.setLocation(meeting.getLocation());

        // Resolve user name
        User user = userRepo.findById(new ObjectId(meeting.getAssignedToId())).orElse(null);
        dto.setAssignedTo(user != null ? user.getUsername() : "Unassigned");

        // Get note content(s)
        if (meeting.getNote() != null) {
            dto.setNotes(List.of(meeting.getNote().getContent()));
        } else {
            dto.setNotes(Collections.emptyList());
        }

        return dto;
    }
}
