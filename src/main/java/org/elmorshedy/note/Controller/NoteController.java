package org.elmorshedy.note.Controller;

import org.bson.types.ObjectId;
import org.elmorshedy.note.ServiceImp.NoteServiceImp;
import org.elmorshedy.note.models.Note;
import org.elmorshedy.note.models.NoteRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private NoteServiceImp noteService;

    @Autowired
    public NoteController(NoteServiceImp noteService) {
        this.noteService = noteService;
    }

    @PostMapping("/lead/{leadId}")
    public ResponseEntity<Note> createnote(
            @PathVariable ObjectId leadId,
            @RequestBody NoteRequest noteRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        String content = noteRequest.getContent();
        Note createdNote = noteService.createNoteForLead(leadId, content, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNote);
    }

    @PutMapping("/{noteid}")
    public ResponseEntity<Note> updateNote(
            @PathVariable ObjectId noteid,
            @RequestBody NoteRequest noteRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        String contant = noteRequest.getContent();
        return ResponseEntity.ok(noteService.updateNote(noteid, contant));
    }

    @GetMapping("/lead/{leadId}")
    public ResponseEntity<List<Note>> getNotesByLead(@PathVariable ObjectId leadId) {
        return ResponseEntity.ok(noteService.getNotesByLead(leadId));
    }

    @DeleteMapping("/{noteid}")
    public ResponseEntity<Void> deleteNote(@PathVariable ObjectId noteid) {
        noteService.deleteById(noteid);
        return ResponseEntity.noContent().build();
    }
}

