package org.elmorshedy.meeting.controller;

import org.bson.types.ObjectId;
import org.elmorshedy.meeting.model.Meeting;
import org.elmorshedy.meeting.model.MeetingDTO;
import org.elmorshedy.meeting.model.MeetingRequest;
import org.elmorshedy.meeting.service.MeetingMapper;
import org.elmorshedy.meeting.service.MeetingServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingServiceImp meetingService;

    @Autowired
    public MeetingController(MeetingServiceImp meetingService, MeetingMapper meetingMapper) {
        this.meetingService = meetingService;
    }

    @PostMapping
    public ResponseEntity<MeetingDTO> createMeeting(@RequestBody MeetingRequest request) {
        MeetingDTO created = meetingService.addMeeting(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<MeetingDTO>> getAllMeetings() {
        return ResponseEntity.ok(meetingService.getAllMeetings1());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MeetingDTO>> getMeetingsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(meetingService.getMeetingsByUser(userId));
    }

    @PutMapping("/{meetingId}")
    public ResponseEntity<Meeting> updateMeeting(@PathVariable ObjectId meetingId, @RequestBody MeetingRequest request) {
        Meeting updated = meetingService.updateMeeting(meetingId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{meetingId}")
    public ResponseEntity<Void> deleteMeeting(@PathVariable ObjectId meetingId) {
        meetingService.deleteMeeting(meetingId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler({NoSuchElementException.class})
    public ResponseEntity<String> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
