package com.jobportal.service;

import com.jobportal.enums.ApplicationStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendStatusUpdate(String toEmail,
                                 String candidateName,
                                 String jobTitle,
                                 String company,
                                 ApplicationStatus status){
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Application Update - "+ jobTitle+ " at "+ company);
            helper.setText(buildEmailBody(candidateName, jobTitle, company, status), true);

            mailSender.send(message);
            log.info("Email sent to {}", toEmail);


        }catch(MessagingException e){
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildEmailBody(String name,
                                  String jobTitle,
                                  String company,
                                  ApplicationStatus status) {
        String statusMessage = switch (status) {
            case SHORTLISTED -> "🎉 Great news! You have been <b>shortlisted</b> for the next round.";
            case REJECTED    -> "Thank you for your interest. Unfortunately, you were <b>not selected</b> this time.";
            case HIRED       -> "🎊 Congratulations! You have been <b>hired</b>!";
            default          -> "Your application status has been updated to <b>" + status + "</b>.";
        };

        return """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <div style="background: #1a56a0; padding: 24px; border-radius: 8px 8px 0 0;">
                        <h2 style="color: white; margin: 0;">Job Portal</h2>
                    </div>
                    <div style="padding: 32px; background: #f9f9f9; border: 1px solid #eee;">
                        <p>Hi <b>%s</b>,</p>
                        <p>%s</p>
                        <div style="background: white; padding: 16px; border-radius: 8px;
                                    border-left: 4px solid #1a56a0; margin: 20px 0;">
                            <p style="margin: 0;"><b>Position:</b> %s</p>
                            <p style="margin: 8px 0 0;"><b>Company:</b> %s</p>
                            <p style="margin: 8px 0 0;"><b>Status:</b> %s</p>
                        </div>
                        <p style="color: #888; font-size: 13px;">
                            This is an automated email from Job Portal.
                        </p>
                    </div>
                </div>
                """.formatted(name, statusMessage, jobTitle, company, status);
    }
}
