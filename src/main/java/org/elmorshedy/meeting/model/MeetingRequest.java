package org.elmorshedy.meeting.model;

import lombok.Data;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class MeetingRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Client ID is required")
    private String clientId;

    @NotBlank(message = "AssignedTo ID is required")
    private String assignedToId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Time is required")
    private LocalTime time;

    @Min(1) @Max(8)
    private Integer duration;

    @NotNull(message = "Type is required")
    private Type type;

    @NotNull(message = "Status is required")
    private Status status;

    @NotNull(message = "Location is required")
    private Location location;

    private List<String> notes;

}