package org.elmorshedy.meeting.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.HashIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private String offline_location;
    @HashIndexed
    @JsonIgnore
    private ObjectId clientId;
    @HashIndexed
    @JsonIgnore
    private ObjectId assignedToId;

    private List<String> notes = new ArrayList<>();
}