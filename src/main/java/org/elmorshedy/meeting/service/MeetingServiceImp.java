package org.elmorshedy.meeting.service;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import org.elmorshedy.lead.repo.LeadRepo;
import org.elmorshedy.meeting.model.*;
import org.elmorshedy.meeting.repo.MeetingRepo;
import org.elmorshedy.user.model.AppRole;
import org.elmorshedy.user.model.User;
import org.elmorshedy.user.repo.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MeetingServiceImp implements MeetingService {

    private final MeetingRepo meetingRepo;
    private final LeadRepo leadRepo;
    private final MeetingMapper meetingMapper;
    private final UserRepo userRepo;

    public MeetingServiceImp(MeetingRepo meetingRepo,
                             LeadRepo leadRepo,
                             MeetingMapper meetingMapper,
                             UserRepo userRepo) {
        this.meetingRepo = meetingRepo;
        this.leadRepo = leadRepo;
        this.meetingMapper = meetingMapper;
        this.userRepo = userRepo;
    }

    private Meeting createMeetingFromRequest(MeetingRequest request) {
        Meeting meeting = new Meeting();
        meeting.setTitle(request.getTitle());
        meeting.setDate(request.getDate());
        meeting.setTime(request.getTime());
        meeting.setDuration(request.getDuration());
        meeting.setType(request.getType());
        meeting.setLocation(request.getLocation());
        meeting.setStatus(request.getStatus());

        if (request.getNotes() != null) {
            meeting.setNotes(request.getNotes());
        }
        return meeting;
    }

    private void validateMeeting(Meeting meeting) {
        if (meeting.getDuration() > 8) {
            throw new IllegalArgumentException("Duration cannot be more than 8 hours");
        }
        if (meeting.getDuration() < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }

        LocalDateTime meetingDateTime = LocalDateTime.of(meeting.getDate(), meeting.getTime());

        if (meeting.getStatus().equals(Status.SCHEDULED) && meetingDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Scheduled meeting must be set in the future");
        }

        if (meeting.getStatus().equals(Status.COMPLETED) && meetingDateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Completed meeting must be set in the past");
        }
    }

    private void assignMeetingToUserAndLead(Meeting meeting, User currentUser, MeetingRequest request) {
        boolean isAdmin = isAdmin(currentUser);

        if (isAdmin && request.getClientId() != null) {
            if (!leadRepo.existsById(new ObjectId(request.getClientId()))) {
                throw new NoSuchElementException("Client not found with ID: " + request.getClientId());
            }
            meeting.setClientId(new ObjectId(request.getClientId()));
        } else {
            meeting.setClientId(currentUser.getId());
        }

        if (isAdmin && request.getAssignedToId() != null) {
            if (!userRepo.existsById(new ObjectId(request.getAssignedToId()))) {
                throw new NoSuchElementException("User not found with ID: " + request.getAssignedToId());
            }
            meeting.setAssignedToId(new ObjectId(request.getAssignedToId()));
        } else {
            meeting.setAssignedToId(currentUser.getId());
        }

    }

    @Transactional
    @Override
    public MeetingDTO addMeeting(MeetingRequest request, String currentUsername) {
        User currentUser = userRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NoSuchElementException("Current user not found"));
        Meeting meeting = createMeetingFromRequest(request);
        assignMeetingToUserAndLead(meeting, currentUser, request);
        validateMeeting(meeting);

        Meeting saved = meetingRepo.save(meeting);
        return meetingMapper.toDTO(saved);
    }


    @Override
    public List<MeetingDTO> getAllMeetings() {
        return meetingRepo.findAll().stream()
                .map(meetingMapper::toDTO)
                .collect(Collectors.toList());
    }

    public MeetingDTO getMeetingById(ObjectId id) {
        Meeting meeting = meetingRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Meeting not found with ID: " + id));
        return meetingMapper.toDTO(meeting);
    }

    @Override
    public List<MeetingDTO> getMeetingByuser(ObjectId id) {
        return meetingRepo.findByAssignedToId(id).stream()
                .map(meetingMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteMeeting(ObjectId meetingId) {
        Meeting meeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new NoSuchElementException("Meeting not found with ID: " + meetingId));
        if (!meeting.getStatus().equals(Status.SCHEDULED)) {
            meetingRepo.delete(meeting);
        } else {
            throw new IllegalStateException("Cannot delete a scheduled meeting with ID: " + meetingId);
        }
    }


//    public List<MeetingDTO> getMeetingsByUser(String userId) {
//        return meetingRepo.findByAssignedToId(userId)
//                .stream()
//                .map(meetingMapper::toDTO)
//                .toList();
//    }

    @Transactional
    public MeetingDTO updateMeeting(ObjectId meetingId, MeetingRequest request) {
        // 1. جيب الـ Meeting الحالية
        Meeting updatemeeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new NoSuchElementException("Meeting not found with ID: " + meetingId));
        applyUpdatesToMeeting(request, updatemeeting);

        validateMeeting(updatemeeting);

        Meeting saved = meetingRepo.save(updatemeeting);
        return meetingMapper.toDTO(saved);
    }

    @Transactional
    public MeetingDTO addNoteToMeeting(ObjectId meetingId, String noteContent) {
        Meeting meeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new NoSuchElementException("Meeting not found"));

        if (meeting.getNotes() == null) {
            meeting.setNotes(new ArrayList<>());
        }

        meeting.getNotes().add(noteContent);
        Meeting updated = meetingRepo.save(meeting);

        return meetingMapper.toDTO(updated);
    }

    private void applyUpdatesToMeeting(MeetingRequest request, Meeting meeting) {
        if (request.getTitle() != null) {
            meeting.setTitle(request.getTitle());
        }
        if (request.getDate() != null) {
            meeting.setDate(request.getDate());
        }
        if (request.getTime() != null) {
            meeting.setTime(request.getTime());
        }
        if (request.getDuration() != null) {
            meeting.setDuration(request.getDuration());
        }
        if (request.getType() != null) {
            meeting.setType(request.getType());
        }
        if (request.getStatus() != null) {
            meeting.setStatus(request.getStatus());
        }
        if (request.getLocation() != null) {
            meeting.setLocation(request.getLocation());
        }
        if (request.getNotes() != null) {
            meeting.setNotes(request.getNotes());
        }

        if (request.getClientId() != null) {
            if (!leadRepo.existsById(new ObjectId(request.getClientId()))) {
                throw new NoSuchElementException("Client not found");
            }
            meeting.setClientId(new ObjectId(request.getClientId()));
        }

        if (request.getAssignedToId() != null) {
            if (!userRepo.existsById(new ObjectId(request.getAssignedToId()))) {
                throw new NoSuchElementException("User not found");
            }
            meeting.setAssignedToId(new ObjectId(request.getAssignedToId()));
        }
    }

    public MeetingDTO deleteNoteFromMeeting(ObjectId id, String content) {
        Meeting meeting = meetingRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Meeting not found with ID: " + id));
        if (meeting.getNotes() == null || meeting.getNotes().isEmpty()) {
            throw new IllegalArgumentException("Meeting has no notes to delete");
        }
        boolean remove = meeting.getNotes().removeIf(note -> note.equals(content));

        if (!remove) {
            throw new IllegalArgumentException("Note not found in meeting");
        }
        Meeting updated = meetingRepo.save(meeting);
        return meetingMapper.toDTO(updated);
    }

    private boolean isAdmin(User user) {
        return user.getRole().getRolename().equals(AppRole.ROLE_ADMIN);
    }
}
