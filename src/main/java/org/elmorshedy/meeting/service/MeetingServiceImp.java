package org.elmorshedy.meeting.service;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import org.elmorshedy.lead.repo.LeadRepo;
import org.elmorshedy.meeting.model.*;
import org.elmorshedy.meeting.repo.MeetingRepo;
import org.elmorshedy.user.repo.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    public MeetingDTO getMeetingById(ObjectId id) {
        Meeting meeting = meetingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found with ID: " + id));
        return meetingMapper.toDTO(meeting);
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
        LocalDateTime meetingDateTime = LocalDateTime.of(meeting.getDate(), meeting.getTime());

        if (meetingDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Meeting date/time cannot be in the past");
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
                .orElseThrow(() -> new RuntimeException("Meeting not found with ID: " + meetingId));
        System.out.println("Incoming Request: " + request);
        System.out.println("updatemeeting: " + updatemeeting);

        // 2. apply التحديثات
        applyUpdatesToMeeting(request, updatemeeting);
        System.out.println("updatemeeting: " + updatemeeting);
        // 3. validate البيانات بعد التحديث
        validateMeeting(updatemeeting);

        Meeting saved = meetingRepo.save(updatemeeting);
        return meetingMapper.toDTO(saved);
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
                throw new RuntimeException("Client not found");
            }
            meeting.setClientId(new ObjectId(request.getClientId()));
        }

        if (request.getAssignedToId() != null) {
            if (!userRepo.existsById(new ObjectId(request.getAssignedToId()))) {
                throw new RuntimeException("User not found");
            }
            meeting.setAssignedToId(new ObjectId(request.getAssignedToId()));
        }
    }
}
