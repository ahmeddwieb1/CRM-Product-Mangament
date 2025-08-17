package org.elmorshedy.meeting.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.elmorshedy.lead.model.Lead;
import org.elmorshedy.note.models.Note;
import org.elmorshedy.user.model.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@ToString(exclude = {"assignedTo"})
public class Meeting {
    @Id
    private ObjectId id;

    @NotBlank
    private String title;

    // Client lead
    @DBRef
    private Lead client;

    // Date components
    private int year;
    private int month;
    private int day;

    // Time components
    private int hour;
    private int minutes;

    // Duration in hours (max 2)
    @Max(2)
    private int duration;

    // Enums
    private Type type;
    private Status status;
    private Location location;

    // Assignment and note
    @DBRef
    private User assignedTo;

    @DBRef
    private Note note;
}
