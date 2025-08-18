package org.elmorshedy.meeting.service;

import org.bson.types.ObjectId;
import org.elmorshedy.meeting.model.Meeting;
import org.elmorshedy.meeting.repo.meetingRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class MeetingServiceImp implements MeetingService {

    private final meetingRepo meetingRepo;

    public MeetingServiceImp(meetingRepo meetingRepo) {
        this.meetingRepo = meetingRepo;
    }

    @Override
    public Meeting addMeeting(Meeting meeting) {
        validateMeeting(meeting);
        return meetingRepo.save(meeting);
    }

    @Override
    public List<Meeting> getAllMeetings() {
        return meetingRepo.findAll();
    }

    @Override
    public List<Meeting> getMeetingsByUser(ObjectId userId) {
        return meetingRepo.findByAssignedTo_Id(userId);
    }

    @Override
    public void deleteMeeting(ObjectId meetingId) {
        if (!meetingRepo.existsById(meetingId)) {
            throw new NoSuchElementException("Meeting not found: " + meetingId);
        }
        meetingRepo.deleteById(meetingId);
    }

    @Override
    public Meeting updateMeeting(ObjectId meetingId, Meeting updated) {
        Meeting existing = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new NoSuchElementException("Meeting not found: " + meetingId));

        // Copy fields
        existing.setTitle(updated.getTitle());
        existing.setClient(updated.getClient());
        existing.setYear(updated.getYear());
        existing.setMonth(updated.getMonth());
        existing.setDay(updated.getDay());
        existing.setHour(updated.getHour());
        existing.setMinutes(updated.getMinutes());
        existing.setDuration(updated.getDuration());
        existing.setType(updated.getType());
        existing.setStatus(updated.getStatus());
        existing.setLocation(updated.getLocation());
        existing.setAssignedTo(updated.getAssignedTo());
        existing.setNote(updated.getNote());

        validateMeeting(existing);
        return meetingRepo.save(existing);
    }

    private void validateMeeting(Meeting meeting) {
        if (meeting.getDuration() > 2) {
            throw new IllegalArgumentException("Duration cannot be more than 2 hours");
        }
        if (meeting.getDuration() < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
        // Optionally: add ranges for date/time if desired.
    }
}
