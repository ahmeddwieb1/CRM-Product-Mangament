package org.elmorshedy.lead.model;


import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.elmorshedy.user.model.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document
@ToString(exclude = {"assignedTo"})
public class Lead {
    @Id
    private ObjectId id;

    private String leadName;

    @Size(min = 10, max = 12)
    @Pattern(regexp = "^[0-9]+$", message = "Phone number must contain only digits")
    private String phone;

    @DBRef
    private User assignedTo;

    private double budget;

    private LeadSource leadSource;

    private LeadStatus leadStatus;
//    @DBRef(lazy = true)
//    private List<Note> notes = new ArrayList<>();
}
