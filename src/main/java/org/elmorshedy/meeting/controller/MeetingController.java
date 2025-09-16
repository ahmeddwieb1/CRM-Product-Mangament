package org.elmorshedy.meeting.controller;

import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.elmorshedy.meeting.model.MeetingDTO;
import org.elmorshedy.meeting.model.MeetingRequest;
import org.elmorshedy.meeting.model.NoteRequest;
import org.elmorshedy.meeting.service.MeetingMapper;
import org.elmorshedy.meeting.service.MeetingServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
@RestController
@RequestMapping("/api/meetings")
@Tag(name = "Meetings", description = "APIs for managing meetings")
public class MeetingController {

    private final MeetingServiceImp meetingService;


    @Autowired
    public MeetingController(MeetingServiceImp meetingService) {
        this.meetingService = meetingService;
    }

    @Operation(summary = "Create a new meeting",
            description = "Creates a meeting and returns the created object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Meeting created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    public ResponseEntity<MeetingDTO> createMeeting(@RequestBody @Valid MeetingRequest request,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        MeetingDTO created = meetingService.addMeeting(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get meeting by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meeting found"),
            @ApiResponse(responseCode = "404", description = "Meeting not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MeetingDTO> getMeetingById(@PathVariable ObjectId id) {
        return ResponseEntity.ok(meetingService.getMeetingById(id));
    }

    @Operation(summary = "Get meetings for a specific user")
    @GetMapping("/{id}/user")
    public ResponseEntity<List<MeetingDTO>> getMeetingsByUser(@PathVariable ObjectId id) {
        return ResponseEntity.ok(meetingService.getMeetingByuser(id));
    }

    @Operation(summary = "Get all meetings")
    @GetMapping
    public ResponseEntity<List<MeetingDTO>> getAllMeetings() {
        return ResponseEntity.ok(meetingService.getAllMeetings());
    }

    @Operation(summary = "Update a meeting by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meeting updated successfully"),
            @ApiResponse(responseCode = "404", description = "Meeting not found"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PutMapping("/{meetingId}")
    public ResponseEntity<MeetingDTO> updateMeeting(@PathVariable ObjectId meetingId,
                                                    @RequestBody @Valid MeetingRequest request) {
        MeetingDTO updated = meetingService.updateMeeting(meetingId, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a meeting by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Meeting deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete scheduled meeting"),
            @ApiResponse(responseCode = "404", description = "Meeting not found")
    })
    @DeleteMapping("/{meetingId}")
    public ResponseEntity<Void> deleteMeeting(@PathVariable ObjectId meetingId) {
        meetingService.deleteMeeting(meetingId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add a note to a meeting")
    @PatchMapping("/{meetingId}/notes")
    public ResponseEntity<MeetingDTO> addNoteToMeeting(
            @PathVariable ObjectId meetingId,
            @RequestBody @Valid NoteRequest noteRequest) {
        MeetingDTO updatedMeeting = meetingService.addNoteToMeeting(meetingId, noteRequest.getContent());
        return ResponseEntity.ok(updatedMeeting);
    }

    @Operation(summary = "Delete a note from a meeting")
    @DeleteMapping("/{meetingId}/notes")
    public ResponseEntity<MeetingDTO> deleteNoteFromMeeting(@PathVariable ObjectId meetingId,
                                                            @RequestBody @Valid NoteRequest noteRequest) {
        MeetingDTO updatedMeeting = meetingService.deleteNoteFromMeeting(meetingId, noteRequest.getContent());
        return ResponseEntity.ok(updatedMeeting);
    }

//    @GetMapping("/user/{userId}")
//    public ResponseEntity<List<MeetingDTO>> getMeetingsByUser(@PathVariable String userId) {
//        return ResponseEntity.ok(meetingService.getMeetingsByUser(userId));
//    }
}
