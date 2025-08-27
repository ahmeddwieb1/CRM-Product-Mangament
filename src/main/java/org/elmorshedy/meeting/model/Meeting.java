package org.elmorshedy.meeting.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.types.ObjectId;
import org.elmorshedy.lead.model.Lead;
import org.elmorshedy.user.model.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "meetings")
public class Meeting {
    @Id
    private ObjectId id;

    private String title;
    private LocalDate date;
    private LocalTime time;
    private int duration;

    private Type type;
    private Status status;
    private Location location;

    @JsonIgnore
    private ObjectId clientId;

    @JsonIgnore
    private ObjectId assignedToId;

    private List<String> notes = new ArrayList<>();
}