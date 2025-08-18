package org.elmorshedy.meeting.service;

import org.bson.types.ObjectId;
import org.elmorshedy.meeting.model.Meeting;

import java.util.List;

public interface MeetingService {
    Meeting addMeeting(Meeting meeting);
    List<Meeting> getAllMeetings();
    List<Meeting> getMeetingsByUser(ObjectId userId);
    void deleteMeeting(ObjectId meetingId);
    Meeting updateMeeting(ObjectId meetingId, Meeting updated);
}
