package org.elmorshedy.note.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.elmorshedy.lead.model.Lead;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@NoArgsConstructor
@ToString(exclude = {"lead"})
public class Note {
    @Id
    private ObjectId id;

    private String content;
    @DBRef
    @JsonIgnore
    private Lead lead;

    public Note(String content, Lead lead) {
        this.content = content;
        this.lead = lead;
    }
}
