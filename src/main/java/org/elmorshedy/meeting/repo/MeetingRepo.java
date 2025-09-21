package org.elmorshedy.meeting.repo;

import org.bson.types.ObjectId;
import org.elmorshedy.meeting.model.Meeting;
import org.elmorshedy.meeting.model.MeetingDTO;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingRepo extends MongoRepository<Meeting, ObjectId> {
    Long countByAssignedToId(ObjectId assignedToId);

    void deleteByAssignedToId(ObjectId assignedToId);

    void deleteByClientId(ObjectId clientId);

    List<Meeting> findByAssignedToId(ObjectId assignedToId);
}