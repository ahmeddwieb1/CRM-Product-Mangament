package org.elmorshedy.meeting.service;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import org.elmorshedy.lead.repo.LeadRepo;
import org.elmorshedy.meeting.model.*;
import org.elmorshedy.meeting.repo.MeetingRepo;
import org.elmorshedy.user.repo.UserRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
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

    @Override
    public MeetingDTO addMeeting(MeetingRequest request) {
        Meeting meeting = new Meeting();
        meeting.setTitle(request.getTitle());
        meeting.setDate(request.getDate());
        meeting.setTime(request.getTime());
        meeting.setDuration(request.getDuration());
        meeting.setType(request.getType());
        meeting.setStatus(request.getStatus());
        meeting.setLocation(request.getLocation());

        if (!leadRepo.existsById(new ObjectId(request.getClientId()))) {
            throw new RuntimeException("Client not found with ID: " + request.getClientId());
        }
        meeting.setClientId(new ObjectId(request.getClientId()));

        if (!userRepo.existsById(new ObjectId(request.getAssignedToId()))) {
            throw new RuntimeException("User not found with ID: " + request.getAssignedToId());
        }
        meeting.setAssignedToId(new ObjectId(request.getAssignedToId()));

        if (request.getNotes() != null) {
            meeting.setNotes(request.getNotes());
        }
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
    @Override
    public void deleteMeeting(ObjectId meetingId) {
        Meeting meeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found with ID: " + meetingId));
        meetingRepo.delete(meeting);
        log.info("Deleting meeting: {}", meeting.getTitle());
    }

    private void validateMeeting(Meeting meeting) {
        if (meeting.getDuration() > 8) {
            throw new IllegalArgumentException("Duration cannot be more than 2 hours");
        }
        if (meeting.getDuration() < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
        if (meeting.getDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Meeting date cannot be in the past");
        }
    }

//    public List<MeetingDTO> getMeetingsByUser(String userId) {
//        return meetingRepo.findByAssignedToId(userId)
//                .stream()
//                .map(meetingMapper::toDTO)
//                .toList();
//    }

    //    @Override
//    public Meeting updateMeeting(ObjectId meetingId, MeetingRequest request) {
//        Meeting existing = meetingRepo.findById(meetingId)
//                .orElseThrow(() -> new NoSuchElementException("Meeting not found: " + meetingId));
//        applyRequestToMeetingPartial(request, existing);
//        validateMeeting(existing);
//        return meetingRepo.save(existing);
//    }
}
