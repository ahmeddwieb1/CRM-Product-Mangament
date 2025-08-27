package org.elmorshedy.meeting.service;

import org.bson.types.ObjectId;
import org.elmorshedy.meeting.model.Meeting;
import org.elmorshedy.meeting.model.MeetingDTO;
import org.elmorshedy.meeting.model.MeetingRequest;

import java.util.List;

public interface MeetingService {


    MeetingDTO addMeeting(MeetingRequest request);

    List<MeetingDTO> getAllMeetings();

    void deleteMeeting(ObjectId meetingId);
}
