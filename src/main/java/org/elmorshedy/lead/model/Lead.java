package org.elmorshedy.lead.model;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.bson.types.ObjectId;
import org.elmorshedy.user.model.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Lead {
    @Id
    private ObjectId id;

    private String leadName;
    @Size(min = 10, max = 12)
    @Pattern(regexp = "^[0-9]+$", message = "Phone number must contain only digits")
    private String phone;

    private User user;

    private double budget;

    private LeadSource leadSource;

    private LeadStatus leadStatus;
}
