package org.elmorshedy.meeting.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.elmorshedy.note.models.Note;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Document
@ToString(exclude = {"assignedToId"})
public class Meeting {
    @Id
    private ObjectId id;

    @NotBlank
    private String title;

    private String clientid;

    private LocalDate date;

    private LocalTime time;

    @Max(2)
    private int duration;

    private Type type;
    private Status status;
    private Location location;

    private String assignedToId;

    @DBRef
    private Note note;
}
