package org.elmorshedy.note.Repo;

import org.bson.types.ObjectId;
import org.elmorshedy.note.models.Note;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NoteRepo extends MongoRepository<Note, ObjectId> {

}

