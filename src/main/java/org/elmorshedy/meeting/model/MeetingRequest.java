package org.elmorshedy.meeting.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class MeetingRequest {
    private String title;
    private String clientId;
    private String assignedToId;
    private LocalDate date;
    private LocalTime time;
    private Integer duration;
    private Type type;
    private Status status;
    private String location;

    private String noteMessage;
}
