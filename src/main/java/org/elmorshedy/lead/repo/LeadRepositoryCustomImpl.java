package org.elmorshedy.lead.repo;

import org.bson.Document;
import org.elmorshedy.lead.model.LeadDTO;
import org.elmorshedy.lead.model.LeadSource;
import org.elmorshedy.lead.model.LeadStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Repository;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class LeadRepositoryCustomImpl implements LeadRepositoryCustom {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<LeadDTO> findAllWithUser() {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.lookup("users", "assignedToId", "_id", "user")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(agg, "lead", Document.class);

        return results.getMappedResults().stream()
                .map(this::todto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeadDTO> findAllWithUserWithPage(int page, int size) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.lookup("users", "assignedToId", "_id", "user"),
                Aggregation.skip((long) page * size),
                Aggregation.limit(size)
        );
        AggregationResults<Document> results = mongoTemplate.aggregate(agg, "lead", Document.class);

        return results.getMappedResults().stream()
                .map(this::todto)
                .collect(Collectors.toList());
    }

    private LeadDTO todto(Document doc) {
        LeadDTO dto = new LeadDTO();

        dto.setId(doc.getObjectId("_id").toString());
        dto.setLeadName(doc.getString("leadName"));
        dto.setPhone(doc.getString("phone"));
        dto.setBudget(doc.getDouble("budget"));

        dto.setLeadStatus(LeadStatus.valueOf(doc.getString("leadStatus")));
        dto.setLeadSource(LeadSource.valueOf(doc.getString("leadSource")));

        dto.setNotes((List<String>) doc.get("notes"));

        List<Document> users = (List<Document>) doc.get("user");
        if (users != null && !users.isEmpty()) {
            dto.setAssignedToName(users.get(0).getString("username"));
        }
        return dto;
    }
}
