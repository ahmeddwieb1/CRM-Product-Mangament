package org.elmorshedy.lead.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Leads", description = "APIs for managing leads")
public class LeadController {

    private final LeadServiceImp leadService;

    @Autowired
    public LeadController(LeadServiceImp leadService) {
        this.leadService = leadService;
    }

    @Operation(summary = "Add a new lead", description = "Creates a new lead and assigns it to a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lead created successfully",
                    content = @Content(schema = @Schema(implementation = LeadDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(example = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Validation error\"}")))
    })
    @PostMapping
    public ResponseEntity<LeadDTO> addLead(
            @Valid @RequestBody RequestLead leadRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        LeadDTO savedLead = leadService.addLead(leadRequest, userDetails.getUsername());
        return ResponseEntity.ok(savedLead);
    }

    @Operation(summary = "Update an existing lead (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lead updated successfully",
                    content = @Content(schema = @Schema(implementation = LeadDTO.class))),
            @ApiResponse(responseCode = "404", description = "Lead not found",
                    content = @Content(schema = @Schema(example = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Lead not found\"}")))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<LeadDTO> updateLead(
            @PathVariable ObjectId id,
            @RequestBody @Valid RequestLead leadRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        LeadDTO updatedLead = leadService.updateLead(id, leadRequest, userDetails.getUsername());
        return ResponseEntity.ok(updatedLead);
    }

    @Operation(summary = "Update lead status only")
    @PatchMapping("/{id}")
    public ResponseEntity<LeadDTO> updateStatus(
            @PathVariable ObjectId id,
            @RequestBody RequestLead leadRequest) {
        LeadDTO updatedLead = leadService.updateLeadStatus(id, leadRequest);
        return ResponseEntity.ok(updatedLead);
    }

    @Operation(summary = "Get lead by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lead found",
                    content = @Content(schema = @Schema(implementation = LeadDTO.class))),
            @ApiResponse(responseCode = "404", description = "Lead not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LeadDTO> getLeadById(@PathVariable ObjectId id) {
        return leadService.getLead(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all leads assigned to a specific user")
    @GetMapping("/{assignedToId}/user")
    public ResponseEntity<List<LeadDTO>> getLeadsByAssignedToId(@PathVariable ObjectId assignedToId) {
        List<LeadDTO> leads = leadService.getLeadsForSales(assignedToId);
        return ResponseEntity.ok(leads);
    }

    @Operation(summary = "Get all leads (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<LeadDTO> getAllLeads() {
        return leadService.getAllLeads();
    }

    @Operation(summary = "Delete a lead (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLead(@PathVariable ObjectId id) {
        leadService.deleteLead(id);
        return ResponseEntity.ok("Lead deleted successfully");
    }

    @Operation(summary = "Add note to a lead")
    @PatchMapping("/{leadId}/notes")
    public ResponseEntity<LeadDTO> addNoteToLead(
            @PathVariable ObjectId leadId,
            @RequestBody NoteRequest noteRequest) {
        LeadDTO updatedLead = leadService.addNoteToLead(leadId, noteRequest.getContent());
        return ResponseEntity.ok(updatedLead);
    }

    @Operation(summary = "Delete note from a lead")
    @DeleteMapping("/{leadId}/notes")
    public ResponseEntity<LeadDTO> deleteNoteFromLead(
            @PathVariable ObjectId leadId,
            @RequestBody NoteRequest noteRequest) {
        LeadDTO updatedLead = leadService.deleteNoteFromLead(leadId, noteRequest.getContent());
        return ResponseEntity.ok(updatedLead);
    }
}


