package org.elmorshedy.meeting.service;

import org.bson.types.ObjectId;
import org.elmorshedy.lead.model.Lead;
import org.elmorshedy.lead.repo.LeadRepo;
import org.elmorshedy.meeting.model.Location;
import org.elmorshedy.meeting.model.Meeting;
import org.elmorshedy.meeting.model.MeetingDTO;
import org.elmorshedy.meeting.model.MeetingRequest;
import org.elmorshedy.meeting.repo.meetingRepo;
import org.elmorshedy.note.Repo.NoteRepo;
import org.elmorshedy.note.models.Note;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class MeetingServiceImp implements MeetingService {

    private final meetingRepo meetingRepo;
    private final NoteRepo noteRepo;
    private final LeadRepo leadRepo;
    private final MeetingMapper meetingMapper;

    public MeetingServiceImp(meetingRepo meetingRepo,
                             NoteRepo noteRepo,
                             LeadRepo leadRepo,
                             MeetingMapper meetingMapper) {
        this.meetingRepo = meetingRepo;
        this.noteRepo = noteRepo;
        this.leadRepo = leadRepo;
        this.meetingMapper = meetingMapper;
    }

    // New DTO-based create
    @Override
    public MeetingDTO  addMeeting(MeetingRequest request) {
        Meeting meeting = new Meeting();
        applyRequestToMeeting(request, meeting);
        validateMeeting(meeting);
        Meeting saved =
                meetingRepo.save(meeting);
        return meetingMapper.toDTO(saved);
    }

    public List<MeetingDTO> getAllMeetings() {
        return meetingRepo.findAll()
                .stream()
                .map(meetingMapper::toDTO)
                .toList();
    }
    public List<MeetingDTO> getAllMeetings1() {
        return meetingRepo.findAll().stream()
                .map(meetingMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<MeetingDTO> getMeetingsByUser(String userId) {
        return meetingRepo.findByAssignedToId(userId)
                .stream()
                .map(meetingMapper::toDTO)
                .toList();
    }

    @Override
    public void deleteMeeting(ObjectId meetingId) {
        // Load the meeting or fail fast if not found
        Meeting meeting = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new NoSuchElementException("Meeting not found: " + meetingId));

        // If the meeting has an associated note, delete it too and unlink from lead if present
        Note note = meeting.getNote();
        if (note != null && note.getId() != null) {
            try {
                Note noteEntity = noteRepo.findById(note.getId()).orElse(null);
                if (noteEntity != null) {
                    if (noteEntity.getLead() != null) {
                        Lead lead = noteEntity.getLead();
                        if (lead.getNotes() != null) {
                            lead.getNotes().removeIf(n -> n != null && n.getId() != null && n.getId().equals(noteEntity.getId()));
                        }
                        leadRepo.save(lead);
                    }
                    noteRepo.deleteById(noteEntity.getId());
                }
            } catch (Exception ignored) {
                // Best-effort cleanup of note; proceed with meeting deletion even if note deletion fails
            }
        }

        // Now delete the meeting itself
        meetingRepo.deleteById(meetingId);
    }

   // New DTO-based update
    @Override
    public Meeting updateMeeting(ObjectId meetingId, MeetingRequest request) {
        Meeting existing = meetingRepo.findById(meetingId)
                .orElseThrow(() -> new NoSuchElementException("Meeting not found: " + meetingId));
        applyRequestToMeetingPartial(request, existing);
        validateMeeting(existing);
        return meetingRepo.save(existing);
    }

    private void applyRequestToMeeting(MeetingRequest request, Meeting target) {
        if (request == null) return;
        if (request.getTitle()!=null && !request.getTitle().isEmpty())
            target.setTitle(request.getTitle());

        target.setClientid(request.getClientId());
        target.setDate(request.getDate());
        target.setTime(request.getTime());
        if (request.getDuration() != null) target.setDuration(request.getDuration());
        target.setType(request.getType());
        target.setStatus(request.getStatus());
        // Location in request is a String; parse to enum safely
        if (request.getLocation() != null) {
            try {
                target.setLocation(Location.valueOf(request.getLocation().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid location value: " + request.getLocation());
            }
        } else {
            target.setLocation(null);
        }
        target.setAssignedToId(request.getAssignedToId());

        // Handle optional noteMessage: create a Note and link if provided
        if (request.getNoteMessage() != null && !request.getNoteMessage().isBlank()) {
            Note note = new Note();
            note.setContent(request.getNoteMessage());
            // Try to link to lead if clientId is a valid ObjectId and exists
            if (request.getClientId() != null) {
                try {
                    ObjectId leadId = new ObjectId(request.getClientId());
                    leadRepo.findById(leadId).ifPresent(note::setLead);
                } catch (IllegalArgumentException ignored) {
                    // clientId not a valid ObjectId; leave lead null
                }
            }
            Note saved = noteRepo.save(note);
            // If linked to a lead, also add to its notes collection
            if (note.getLead() != null) {
                note.getLead().getNotes().add(saved);
                leadRepo.save(note.getLead());
            }
            target.setNote(saved);
        }
    }

    // Applies only non-null (and non-empty for strings) values from request to target.
    private void applyRequestToMeetingPartial(MeetingRequest request, Meeting target) {
        if (request == null) return;
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            target.setTitle(request.getTitle());
        }
        if (request.getClientId() != null) {
            target.setClientid(request.getClientId());
        }
        if (request.getDate() != null) {
            target.setDate(request.getDate());
        }
        if (request.getTime() != null) {
            target.setTime(request.getTime());
        }
        if (request.getDuration() != null) {
            target.setDuration(request.getDuration());
        }
        if (request.getType() != null) {
            target.setType(request.getType());
        }
        if (request.getStatus() != null) {
            target.setStatus(request.getStatus());
        }
        if (request.getLocation() != null) {
            try {
                target.setLocation(Location.valueOf(request.getLocation().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid location value: " + request.getLocation());
            }
        }
        if (request.getAssignedToId() != null && !request.getAssignedToId().isBlank()) {
            target.setAssignedToId(request.getAssignedToId());
        }

        // Only create/link a note if a non-blank noteMessage is provided
        if (request.getNoteMessage() != null && !request.getNoteMessage().isBlank()) {
            Note note ;
            if (target.getNote() != null) {
                note = target.getNote();
            } else {
                note = new Note();
            }
                note.setContent(request.getNoteMessage());
            if (request.getClientId() != null) {
                try {
                    ObjectId leadId = new ObjectId(request.getClientId());
                    Lead lead = leadRepo.findById(leadId).orElse(null);
                    if (lead != null) {
                        note.setLead(lead);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
            Note saved = noteRepo.save(note);
            if (note.getLead() != null) {
                note.getLead().getNotes().add(saved);
                leadRepo.save(note.getLead());
            }
            target.setNote(saved);
        }
    }

    private void validateMeeting(Meeting meeting) {
        if (meeting.getDuration() > 2) {
            throw new IllegalArgumentException("Duration cannot be more than 2 hours");
        }
        if (meeting.getDuration() < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
        // Optionally: add ranges for date/time if desired.
    }
}
