package org.elmorshedy.note.Service;

import org.bson.types.ObjectId;
import org.elmorshedy.note.models.Note;

import java.util.List;

public interface NoteServices {
    Note createNoteForLead(ObjectId leadId, String content, String username);

    List<Note> getNotesByLead(ObjectId leadId);

    void deleteById(ObjectId noteid);

    Note updateNote(ObjectId noteid, String content);

//todo
//    Note getuserbyusername(String username);
}
