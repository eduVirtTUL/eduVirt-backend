package pl.lodz.p.it.eduvirt.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailHelper {

    @Value("${spring.mail.sender}")
    private String fromAddress;

    private final JavaMailSender mailSender;

    public void sendSimpleMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendMimeMail(String to, String subject, String htmlMailContent) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
//        mimeMessage.setHeader("Content-Type", "text/plain; charset=\"utf-8\"");
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        messageHelper.setFrom(fromAddress);
        messageHelper.setTo(to);
        messageHelper.setSubject(subject);
        messageHelper.setText(htmlMailContent, true);
        mailSender.send(mimeMessage);
    }
}
