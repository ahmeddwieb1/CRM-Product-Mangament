package org.elmorshedy.meeting.controller;

import jakarta.validation.Valid;
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
    public ResponseEntity<MeetingDTO> createMeeting(@RequestBody @Valid MeetingRequest request) {
        MeetingDTO created = meetingService.addMeeting(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeetingDTO> getmeetingbyid(@PathVariable ObjectId id){
        return ResponseEntity.ok(meetingService.getMeetingById(id));
    }
    @GetMapping
    public ResponseEntity<List<MeetingDTO>> getAllMeetings() {
        return ResponseEntity.ok(meetingService.getAllMeetings());
    }

//    @GetMapping("/user/{userId}")
//    public ResponseEntity<List<MeetingDTO>> getMeetingsByUser(@PathVariable String userId) {
//        return ResponseEntity.ok(meetingService.getMeetingsByUser(userId));
//    }

    @PutMapping("/{meetingId}")
    public ResponseEntity<MeetingDTO> updateMeeting(@PathVariable ObjectId meetingId, @RequestBody MeetingRequest request) {
        MeetingDTO updated = meetingService.updateMeeting(meetingId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{meetingId}")
    public ResponseEntity<Void> deleteMeeting(@PathVariable ObjectId meetingId) {
        meetingService.deleteMeeting(meetingId);
        return ResponseEntity.noContent().build();
    }
}
