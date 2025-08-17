package org.elmorshedy.note.ServiceImp;

import org.bson.types.ObjectId;
import org.elmorshedy.lead.model.Lead;
import org.elmorshedy.lead.repo.LeadRepo;
import org.elmorshedy.note.Repo.NoteRepo;
import org.elmorshedy.note.Service.NoteServices;
import org.elmorshedy.note.models.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteServiceImp implements NoteServices {
    private final NoteRepo noteRepo;
    private final LeadRepo leadRepo;

    @Autowired
    public NoteServiceImp(NoteRepo noteRepo, LeadRepo leadRepo) {
        this.noteRepo = noteRepo;
        this.leadRepo = leadRepo;
    }

    @Override
    public Note createNoteForLead(ObjectId leadId, String content, String username) {
        Lead lead = leadRepo.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        Note note = new Note();
        note.setContent(content);
        note.setLead(lead);

        Note savedNote = noteRepo.save(note);

        lead.getNotes().add(savedNote);
        leadRepo.save(lead);

        return savedNote;
    }


    @Override
    public List<Note> getNotesByLead(ObjectId leadId) {
        Lead lead = leadRepo.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        return lead.getNotes();
    }

    @Override
    public void deleteById(ObjectId noteid) {
        noteRepo.deleteById(noteid);
    }

    @Override
    public Note updateNote(ObjectId noteid, String content) {
        Note note = noteRepo.findById(noteid)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        note.setContent(content);
        return noteRepo.save(note);
    }
}
