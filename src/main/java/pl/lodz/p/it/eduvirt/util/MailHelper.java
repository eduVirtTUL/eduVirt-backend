package pl.lodz.p.it.eduvirt.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MailHelper {

    @Value("${spring.mail.sender}")
    private String fromAddress;

    private final TemplateEngine templateEngine;
    private final JavaMailSender mailSender;

    /* Mail sending methods */

    @Async
    @PreAuthorize("permitAll()")
    public void sendHtmlEmail(
            String subject,
            String emailTo,
            String emailTemplate,
            Map<String, Object> templateModel,
            String language) {
        try {
            Context thymeleafContext = new Context(Locale.of(language));
            thymeleafContext.setVariables(templateModel);
            String htmlBody = templateEngine.process(emailTemplate, thymeleafContext);
            this.sendEmail(subject, htmlBody, emailTo);
        } catch (Exception exception) {
            log.error("Exception of type: {} was throw while sending e-mail message. Reason: {}",
                    exception.getClass().getSimpleName(), exception.getMessage());
        }
    }

    @PreAuthorize("permitAll()")
    public void sendEmail(String emailSubject, String emailContent, String emailTo) {
        MimeMessage message  = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(emailTo);
            helper.setFrom(fromAddress);
            helper.setSubject(emailSubject);
            helper.setText(emailContent, true);
            mailSender.send(message);
        } catch (MessagingException exception) {
            log.warn("Exception: {} occurred while sending email message, since: {}",
                    exception.getClass().getSimpleName(), exception.getMessage());
        }
    }
}
