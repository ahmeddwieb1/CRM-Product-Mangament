package org.elmorshedy.meeting.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
@Data
public class MeetingDTO {
    private String id;
    private String title;
    private String clientName;
    private LocalDate date;
    private LocalTime time;
    private int duration;
    private Type type;
    private Status status;
    private Location location;
    private String assignedTo;
    private List<String> notes;
}
