package org.elmorshedy.meeting.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoteRequest {
    @NotBlank(message = "Note content is required")
    private String content;
}