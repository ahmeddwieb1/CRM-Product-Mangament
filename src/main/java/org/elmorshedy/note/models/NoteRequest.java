package org.elmorshedy.note.models;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NoteRequest {
    @NotBlank
    private String username;
    @NotNull
    private Gender gender;
    @Email
    private String email;

    @Size(min = 10, max = 12)
    @Pattern(regexp = "^[0-9]+$", message = "Phone number must contain only digits")
    @NotBlank
    private String phone;

    private String content;
}