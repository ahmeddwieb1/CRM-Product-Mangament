package org.elmorshedy.lead.model;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.elmorshedy.user.model.User;
import org.springframework.data.mongodb.core.mapping.DBRef;
@Data
public class UpdateLead {

    private String leadName;

    @Size(min = 10, max = 12)
    @Pattern(regexp = "^[0-9]+$", message = "Phone number must contain only digits")
    private String phone;

    private String assignedTo;

    @Positive(message = "Budget must be greater than 0")
    private double budget;

    private LeadSource leadSource;

    private LeadStatus leadStatus;
}
