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
        dto.setId(meeting.getId().toString());
        dto.setTitle(meeting.getTitle());
        dto.setDate(meeting.getDate());
        dto.setTime(meeting.getTime());
        dto.setDuration(meeting.getDuration());
        dto.setType(meeting.getType());
        dto.setStatus(meeting.getStatus());
        dto.setLocation(meeting.getLocation());
        dto.setNotes(meeting.getNotes());

        if (meeting.getClientId() != null) {
            Lead client = leadRepo.findById(meeting.getClientId()).orElse(null);
            dto.setClientName(client != null ? client.getLeadName() : "Unknown Client");
        }

        if (meeting.getAssignedToId() != null) {
            User user = userRepo.findById(meeting.getAssignedToId()).orElse(null);
            dto.setAssignedTo(user != null ? user.getUsername() : "Unknown User");
        }

        return dto;
    }
}
