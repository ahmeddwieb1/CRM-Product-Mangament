package org.elmorshedy.lead.controller;

import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.elmorshedy.lead.model.RequestLead;
import org.elmorshedy.lead.model.LeadDTO;
import org.elmorshedy.lead.service.LeadServiceImp;
import org.elmorshedy.meeting.model.NoteRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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


    private final LeadServiceImp leadService;

    @Autowired
    public LeadController(LeadServiceImp leadService) {
        this.leadService = leadService;
    }

    @PostMapping
    public ResponseEntity<LeadDTO> addLead(@Valid @RequestBody RequestLead leadRequest, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            LeadDTO savedLead = leadService.addLead(leadRequest, userDetails.getUsername());
            return ResponseEntity.ok(savedLead);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body((LeadDTO) Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLead(@PathVariable ObjectId id,
                                        @RequestBody @Valid RequestLead leadRequest,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            LeadDTO updatedLead = leadService.updateLead(id, leadRequest, userDetails.getUsername());
            return ResponseEntity.ok(updatedLead);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }//todo check if it work
    @PatchMapping("/{id}")
    public ResponseEntity<?> updatestatus(@PathVariable ObjectId id,
                                        @RequestBody RequestLead leadRequest) {
        try {
            LeadDTO updatedLead = leadService.updateLeadStatus(id, leadRequest);
            return ResponseEntity.ok(updatedLead);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeadDTO> getLeadById(@PathVariable ObjectId id) {
        return leadService.getLead(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<LeadDTO> getlead() {
        return leadService.getAllLeads();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLead(@PathVariable ObjectId id) {
        leadService.deleteLead(id);
        return ResponseEntity.ok("Lead deleted successfully");
    }

    @PatchMapping("/{leadId}/notes")
    public ResponseEntity<LeadDTO> addNoteTolead(@PathVariable ObjectId leadId,
                                                 @RequestBody NoteRequest noteRequest) {
        LeadDTO updatedMeeting = leadService.addNoteToLead(leadId, noteRequest.getContent());
        return ResponseEntity.ok(updatedMeeting);
    }

    @DeleteMapping("/{leadId}/notes")
    public ResponseEntity<LeadDTO> deleteNoteFromMeeting(@PathVariable ObjectId leadId,
                                                         @RequestBody NoteRequest noteRequest) {
        LeadDTO updatedMeeting = leadService.deleteNoteFromLead(leadId, noteRequest.getContent());
        return ResponseEntity.ok(updatedMeeting);
    }
}
