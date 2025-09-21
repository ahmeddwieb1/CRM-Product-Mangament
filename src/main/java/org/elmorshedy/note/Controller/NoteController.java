package org.elmorshedy.note.Controller;

import org.bson.types.ObjectId;
import org.elmorshedy.AI.AiService;
import org.elmorshedy.note.Service.NoteServiceImp;
import org.elmorshedy.note.models.Note;
import org.elmorshedy.note.models.NoteRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private NoteServiceImp noteService;
    @Autowired
    public NoteController(NoteServiceImp noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createNote(@RequestBody NoteRequest noteRequest) {

        String reply = noteService.getReply(noteRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Massage sended successfully");
        response.put("aiResponse", reply);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/reply")
    public ResponseEntity<String> getReply(@RequestBody NoteRequest noteRequest) {
        String reply = noteService.getReply(noteRequest);
        return ResponseEntity.ok(reply);
    }

}
