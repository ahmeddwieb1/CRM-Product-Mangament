package org.elmorshedy.note.Controller;

import org.bson.types.ObjectId;
import org.elmorshedy.note.Service.NoteServiceImp;
import org.elmorshedy.note.models.Note;
import org.elmorshedy.note.models.NoteRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private NoteServiceImp noteService;

    @Autowired
    public NoteController(NoteServiceImp noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ResponseEntity<?> createnote(@RequestBody NoteRequest noteRequest) {
        Note createdNote = noteService.createNote(noteRequest);
        String reply = noteService.getReply(noteRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(reply);
    }

    @DeleteMapping("/{noteid}")
    public ResponseEntity<Void> deleteNote(@PathVariable ObjectId noteid) {
        noteService.deleteById(noteid);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/reply")
    public ResponseEntity<String> getReply(@RequestBody NoteRequest noteRequest) {
        String reply = noteService.getReply(noteRequest);
        return ResponseEntity.ok(reply);
    }

}
