package org.elmorshedy.lead.controller;

import org.bson.types.ObjectId;
import org.elmorshedy.lead.model.Lead;
import org.elmorshedy.lead.service.LeadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lead")
public class LeadController {

    @Autowired
    LeadService leadService;

    @PostMapping
    public ResponseEntity<Lead> addLead(@RequestBody Lead lead
//                                        ,@AuthenticationPrincipal UserDetails userDetails
    ) {
        Lead createlead = leadService.addLead(lead);
        return ResponseEntity.status(HttpStatus.CREATED).body(createlead);
    }
    @GetMapping("/{id}")
    public Lead getLeadById(@PathVariable ObjectId id) {
        return leadService.getLead(id);
    }
    @GetMapping
    public List<Lead> getlead(){
        return leadService.getLeads();
    }

}
