package org.elmorshedy.email.service;

import org.elmorshedy.email.model.EmailDetails;

public interface EmailService {
    String sendMailWithAttachment(EmailDetails details);
    String sendSimpleMail(EmailDetails details);
}
