package org.elmorshedy.email.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Timestamp;

@Data
//@AllArgsConstructor
@NoArgsConstructor
public class EmailResponse {
    private String message;
    private String recipient;
    private String emailType;
//    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    public EmailResponse(String message, String recipient, String emailType) {
        this.message = message;
        this.recipient = recipient;
        this.emailType = emailType;
    }
}