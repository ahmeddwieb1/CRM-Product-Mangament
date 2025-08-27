package org.elmorshedy.lead.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class LeadDTO {
    private String id;
    private String leadName;
    private String phone;
    private double budget;
    private String leadSource;
    private String leadStatus;
    private String assignedTo; 
//    private List<String> notes;


}
