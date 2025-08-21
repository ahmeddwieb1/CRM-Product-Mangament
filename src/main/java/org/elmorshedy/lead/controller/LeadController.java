package org.elmorshedy.lead.controller;

import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.elmorshedy.lead.model.Lead;
import org.elmorshedy.lead.model.CreateLead;
import org.elmorshedy.lead.model.LeadDTO;
import org.elmorshedy.lead.model.UpdateLead;
import org.elmorshedy.lead.service.LeadServiceImp;
import org.elmorshedy.user.repo.RoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lead")
public class LeadController {

    @Autowired
    LeadServiceImp leadService;

    //done
    @PostMapping
    public ResponseEntity<?> addLead(@Valid @RequestBody CreateLead leadRequest, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Lead savedLead = leadService.addLead(leadRequest, userDetails.getUsername());
            return ResponseEntity.ok(savedLead);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLead(@PathVariable ObjectId id, @RequestBody UpdateLead leadRequest, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Lead updatedLead = leadService.updateLead(id, leadRequest, userDetails.getUsername());
            return ResponseEntity.ok(updatedLead);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    //done
    @GetMapping("/{id}")
    public ResponseEntity<LeadDTO> getLeadById(@PathVariable ObjectId id) {
        return leadService.getLead(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    //done
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<LeadDTO> getlead() {
        return leadService.getAllLeads();
    }

    @GetMapping("/{userid}/user")
    public List<Lead> getLeadsByUserid(@PathVariable ObjectId userid) {
        return leadService.getLeadsByUserid(userid);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLead(@PathVariable ObjectId id) {
        leadService.deleteLead(id);
        return ResponseEntity.ok("Lead deleted successfully");
    }


}
