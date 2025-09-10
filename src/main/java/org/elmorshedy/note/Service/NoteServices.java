package org.elmorshedy.note.Service;

import org.bson.types.ObjectId;
import org.elmorshedy.note.models.Note;
import org.elmorshedy.note.models.NoteRequest;

import java.util.List;

public interface NoteServices {


    String getReply(NoteRequest noteRequest);
}