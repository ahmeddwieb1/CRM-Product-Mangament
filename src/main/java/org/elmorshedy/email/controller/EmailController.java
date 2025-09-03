package org.elmorshedy.email.controller;

import lombok.extern.slf4j.Slf4j;
import org.elmorshedy.email.model.EmailDetails;
import org.elmorshedy.email.model.EmailResponse;
import org.elmorshedy.email.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@Slf4j
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<EmailResponse> sendMail(@RequestBody EmailDetails details) {
        try {
            log.info("Sending email to: {}", details.getRecipient());

            String status = emailService.sendSimpleMail(details);

            EmailResponse response = new EmailResponse(
                    status,
                    details.getRecipient(),
                    "simple"
            );

            log.info("Email sent successfully to: {}", details.getRecipient());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to send email to: {}. Error: {}",
                    details.getRecipient(), e.getMessage(), e);

            EmailResponse errorResponse = new EmailResponse(
                    "Failed to send email: " + e.getMessage(),
                    details.getRecipient(),
                    "simple"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @PostMapping("/send-with-attachment")
    public ResponseEntity<EmailResponse> sendMailWithAttachment(
            @RequestBody EmailDetails details) {
        try {
            log.info("Sending email with attachment to: {}", details.getRecipient());
            log.debug("Attachment: {}", details.getAttachment());

            String status = emailService.sendMailWithAttachment(details);

            EmailResponse response = new EmailResponse(
                    status,
                    details.getRecipient(),
                    "with_attachment"
            );

            log.info("Email with attachment sent successfully to: {}", details.getRecipient());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to send email with attachment to: {}. Error: {}",
                    details.getRecipient(), e.getMessage(), e);

            EmailResponse errorResponse = new EmailResponse(
                    "Failed to send email with attachment: " + e.getMessage(),
                    details.getRecipient(),
                    "with_attachment"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

}