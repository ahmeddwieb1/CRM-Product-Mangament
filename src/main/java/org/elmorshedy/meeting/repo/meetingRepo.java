package org.elmorshedy.meeting.repo;

import org.bson.types.ObjectId;
import org.elmorshedy.meeting.model.Meeting;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface meetingRepo extends MongoRepository<Meeting, ObjectId> {
    List<Meeting> findByAssignedTo_Id(ObjectId userId);
}