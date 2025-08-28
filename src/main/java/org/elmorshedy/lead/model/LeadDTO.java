package org.elmorshedy.lead.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeadDTO {
    private String id;
    private String leadName;
    private String phone;
    private double budget;
    private LeadSource leadSource;
    private LeadStatus leadStatus;
    private String assignedToName;
    private List<String> notes;
}
