package org.elmorshedy.note.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document
public class Phone {
    @Id
    private ObjectId id;

    @NotBlank
    @Size(min = 10, max = 12)
    @Pattern(regexp = "^[0-9]+$", message = "Phone number must contain only digits")
    private String phone;
}
