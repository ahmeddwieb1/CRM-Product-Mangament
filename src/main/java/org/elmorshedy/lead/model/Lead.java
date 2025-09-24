package org.elmorshedy.lead.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.HashIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document
public class Lead {
    @Id
    private ObjectId id;

    private String leadName;

    @Size(min = 10, max = 12)
    @Pattern(regexp = "^[0-9]+$", message = "Phone number must contain only digits")
    private String phone;
//    @HashIndexed
    @JsonIgnore
    private ObjectId assignedToId;

    private double budget;

    private LeadSource leadSource;

    private LeadStatus leadStatus;

    private List<String> notes = new ArrayList<>();
}
