package org.elmorshedy.lead.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class RequestLead {

    @NotBlank(message = "Lead name is required")
    private String leadName;

    @NotBlank(message = "Phone is required")
    @Size(min = 10, max = 12)
    @Pattern(regexp = "^[0-9]+$", message = "Phone number must contain only digits")
    private String phone;

    @NotNull(message = "Budget is required")
    @Positive(message = "Budget must be greater than 0")
    private double budget;

    @NotNull(message = "Lead source is required")
    private LeadSource leadSource;

    @NotNull(message = "Lead status is required")
    private LeadStatus leadStatus;

    @NotNull(message = "Lead must be assigned to a user")
    private String assignedToId ;

    private List<String> notes;
}
