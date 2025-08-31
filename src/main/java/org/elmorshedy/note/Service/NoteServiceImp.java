package org.elmorshedy.note.Service;

import org.bson.types.ObjectId;
import org.elmorshedy.lead.repo.LeadRepo;
import org.elmorshedy.note.Repo.NoteRepo;
import org.elmorshedy.note.Repo.PhoneRepo;
import org.elmorshedy.note.models.Gender;
import org.elmorshedy.note.models.Note;
import org.elmorshedy.note.models.NoteRequest;
import org.elmorshedy.note.models.Phone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NoteServiceImp implements NoteServices {
    private final NoteRepo noteRepo;
    private final PhoneRepo numberRepo;

    @Autowired
    public NoteServiceImp(NoteRepo noteRepo, LeadRepo leadRepo, PhoneRepo numberRepo) {
        this.noteRepo = noteRepo;
        this.numberRepo = numberRepo;
    }

    private Note createNoteOp(NoteRequest noteRequest) {
        Note note = new Note();
        note.setUsername(noteRequest.getUsername());
        note.setGender(noteRequest.getGender());
        note.setEmail(noteRequest.getEmail());

        Optional<Phone> number = numberRepo.findByPhone(noteRequest.getPhone());
        number.ifPresent(note::setPhone);

        note.setContent(noteRequest.getContent());
        return note;
    }

    public Note createNote(NoteRequest noteRequest) {
        Note note =createNoteOp(noteRequest);
        return noteRepo.save(note);
    }


    @Override
    public void deleteById(ObjectId noteid) {
        noteRepo.deleteById(noteid);
    }

    public String getReply(NoteRequest noteRequest) {
        Optional<Phone> number = numberRepo.findByPhone(noteRequest.getPhone());

        if (number.isPresent() && noteRequest.getGender() == Gender.MALE) {
            return "اخويا الي منورني 🌟";
        }
        else if (number.isPresent() && noteRequest.getGender() == Gender.FEMALE) {
            return "روحي اعملي شاي👋";
        }
        else {
            return "شكرا لزيارتك لموقعي 🙏 ياريت تسيب رأيك.";
        }
    }


}