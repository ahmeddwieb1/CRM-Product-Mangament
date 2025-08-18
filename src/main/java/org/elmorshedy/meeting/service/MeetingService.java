package org.elmorshedy.meeting.service;

import org.bson.types.ObjectId;
import org.elmorshedy.meeting.model.Meeting;
import org.elmorshedy.meeting.model.MeetingRequest;

import java.util.List;

public interface MeetingService {

    // New DTO-based methods using MeetingRequest
    Meeting addMeeting(MeetingRequest request);
    Meeting updateMeeting(ObjectId meetingId, MeetingRequest request);

    List<Meeting> getAllMeetings();
    List<Meeting> getMeetingsByUser(String userId);
    void deleteMeeting(ObjectId meetingId);
}
