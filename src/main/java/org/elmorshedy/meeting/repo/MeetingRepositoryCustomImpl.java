package org.elmorshedy.meeting.repo;

import org.bson.Document;
import org.elmorshedy.meeting.model.Location;
import org.elmorshedy.meeting.model.MeetingDTO;
import org.elmorshedy.meeting.model.Status;
import org.elmorshedy.meeting.model.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Repository;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MeetingRepositoryCustomImpl implements MeetingRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    public List<MeetingDTO> findAllWithUserAndLead() {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.lookup("users", "assignedToId", "_id", "user"),
                Aggregation.lookup("lead", "clientId", "_id", "lead")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(agg, "meetings", Document.class);

        return results.getMappedResults().stream()
                .map(this::todto)
                .collect(Collectors.toList());
    }
    private MeetingDTO todto(Document doc) {
        MeetingDTO dto = new MeetingDTO();
        dto.setId(doc.getObjectId("_id").toString());
        dto.setTitle(doc.getString("title"));

        if (doc.get("date") != null) {
            dto.setDate(doc.getDate("date").toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        if (doc.get("time") != null) {
            dto.setTime(doc.getDate("time").toInstant().atZone(ZoneId.systemDefault()).toLocalTime());
        }

        dto.setDuration(doc.getInteger("duration", 0));
        dto.setType(Type.valueOf(doc.getString("type")));
        dto.setStatus(Status.valueOf(doc.getString("status")));
        dto.setLocation(Location.valueOf(doc.getString("location")));
        dto.setOffline_location(doc.getString("offline_location"));
        dto.setNotes((List<String>) doc.get("notes"));

        // ---- Map nested lead ----
        List<Document> leads = (List<Document>) doc.get("lead");
        if (leads != null && !leads.isEmpty()) {
            dto.setClientName(leads.get(0).getString("leadName"));
        }

        // ---- Map nested user ----
        List<Document> users = (List<Document>) doc.get("user");
        if (users != null && !users.isEmpty()) {
            dto.setAssignedToName(users.get(0).getString("username"));
        }

        return dto;
    }
}
