package org.elmorshedy.note.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@NoArgsConstructor
public class Note {
    @Id
    private ObjectId id;

    @DBRef
    private Phone phone;
    private String username;
    private Gender gender;
    private String email;

    private String content;

    public Note(String content) {
        this.content = content;
    }

}
