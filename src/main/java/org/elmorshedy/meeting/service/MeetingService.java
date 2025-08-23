package org.elmorshedy.meeting.service;

import org.bson.types.ObjectId;
import org.elmorshedy.meeting.model.Meeting;
import org.elmorshedy.meeting.model.MeetingDTO;
import org.elmorshedy.meeting.model.MeetingRequest;

import java.util.List;

public interface MeetingService {

    // New DTO-based methods using MeetingRequest
    MeetingDTO addMeeting(MeetingRequest request);
    Meeting updateMeeting(ObjectId meetingId, MeetingRequest request);

    List<MeetingDTO> getAllMeetings();
    List<MeetingDTO> getMeetingsByUser(String userId);
    void deleteMeeting(ObjectId meetingId);
}
