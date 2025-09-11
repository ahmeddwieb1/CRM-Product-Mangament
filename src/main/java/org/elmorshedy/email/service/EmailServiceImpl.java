package org.elmorshedy.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.elmorshedy.email.model.EmailDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    public String sendSimpleMail(EmailDetails details) {
        try {
            log.debug("Creating simple mail message for: {}", details.getRecipient());

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(sender);
            mailMessage.setTo(details.getRecipient());
            mailMessage.setText(details.getMsgBody());
            mailMessage.setSubject(details.getSubject());

            javaMailSender.send(mailMessage);

            log.debug("Simple email sent successfully to: {}", details.getRecipient());
            return "Mail Sent Successfully";

        } catch (Exception e) {
            log.error("Error sending simple email to: {}. Error: {}",
                    details.getRecipient(), e.getMessage(), e);
            return "Error while Sending Mail: " + e.getMessage();
        }
    }

    public String sendMailWithAttachment(EmailDetails details) {
        log.debug("Attempting to send email with attachment to: {}", details.getRecipient());

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(sender);
            helper.setTo(details.getRecipient());
            helper.setText(details.getMsgBody());
            helper.setSubject(details.getSubject());

            FileSystemResource file = new FileSystemResource(new File(details.getAttachment()));
            helper.addAttachment(file.getFilename(), file);

            javaMailSender.send(mimeMessage);

            log.info("Email with attachment '{}' sent successfully to: {}",
                    file.getFilename(), details.getRecipient());
            return "Mail sent Successfully";

        } catch (MessagingException e) {
            log.error("Messaging exception while sending email to: {}. Error: {}",
                    details.getRecipient(), e.getMessage(), e);
            return "Error while sending mail: " + e.getMessage();
        } catch (Exception e) {
            log.error("Unexpected error while sending email to: {}. Error: {}",
                    details.getRecipient(), e.getMessage(), e);
            return "Unexpected error: " + e.getMessage();
        }
    }
}