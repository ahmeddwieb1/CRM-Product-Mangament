package org.elmorshedy.note.Service;

import org.bson.types.ObjectId;
import org.elmorshedy.note.models.Note;

import java.util.List;

public interface NoteServices {

    void deleteById(ObjectId noteid);

}