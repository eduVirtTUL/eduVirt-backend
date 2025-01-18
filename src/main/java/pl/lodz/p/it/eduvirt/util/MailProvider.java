package pl.lodz.p.it.eduvirt.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.Reservation;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MailProvider {

    @Value("${mail.default.language}")
    private String defaultLanguage;

    @Value("${mail.default.timezone}")
    private String defaultTimezone;

    private final MailHelper mailHelper;
    private final ResourceBundleMessageSource messageSource;

    /* Mail sending methods */

    @PreAuthorize("permitAll()")
    public void sendHtmlTestMessage(String firstName, String lastName,
                                    String emailTo, String timeZone, String language) {
        timeZone = timeZone != null ? timeZone : defaultTimezone;
        language = language != null ? language : defaultLanguage;

        LocalDateTime currentTime = OffsetDateTime.now(ZoneId.of(timeZone)).toLocalDateTime();
        String timestamp = currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Map<String, Object> templateModel = Map.of(
                "firstName", firstName,
                "lastNameName", lastName,
                "currentTime", timestamp
        );

        String subject = messageSource.getMessage("testHtmlMessage.subject", null, Locale.of(language));
        mailHelper.sendHtmlEmail(subject, emailTo, "testTemplate", templateModel, language);
    }

    @PreAuthorize("permitAll()")
    public void sendReservationRemovalEmail(String firstName, String lastName,
                                            String emailTo, Reservation reservation,
                                            String timeZone, String language) {
        timeZone = timeZone != null ? timeZone : defaultTimezone;
        language = language != null ? language : defaultLanguage;

        LocalDateTime startTime = OffsetDateTime.of(reservation.getStartTime(), ZoneOffset.UTC)
                .atZoneSameInstant(ZoneId.of(timeZone)).toLocalDateTime();
        LocalDateTime endTime = OffsetDateTime.of(reservation.getEndTime(), ZoneOffset.UTC)
                .atZoneSameInstant(ZoneId.of(timeZone)).toLocalDateTime();

        String start = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String end = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Map<String, Object> templateModel = Map.of(
                "firstName", firstName,
                "lastName", lastName,
                "teamName", reservation.getTeam().getName(),
                "resourceGroupName", reservation.getResourceGroup().getName(),
                "startTime", start,
                "endTime", end
        );

        String subject = messageSource.getMessage("reservationRemoval.subject", null, Locale.of(language));
        mailHelper.sendHtmlEmail(subject, emailTo, "reservationRemoval", templateModel, language);
    }

    @PreAuthorize("permitAll()")
    public void sendTemporaryCourseResourcesExhaustionEmail(String firstName, String lastName,
                                                            String emailTo, Course course, String resourceName,
                                                            boolean isRg, LocalDateTime start, LocalDateTime end,
                                                            String timeZone, String language) {
        timeZone = timeZone != null ? timeZone : defaultTimezone;
        language = language != null ? language : defaultLanguage;

        LocalDateTime startTime = OffsetDateTime.of(start, ZoneOffset.UTC)
                .atZoneSameInstant(ZoneId.of(timeZone)).toLocalDateTime();
        LocalDateTime endTime = OffsetDateTime.of(end, ZoneOffset.UTC)
                .atZoneSameInstant(ZoneId.of(timeZone)).toLocalDateTime();

        String intervalStart = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String intervalEnd = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("firstName", firstName);
        templateModel.put("lastName", lastName);
        templateModel.put("courseName", course.getName());
        templateModel.put("startTime", intervalStart);
        templateModel.put("endTime", intervalEnd);

        String subject = messageSource.getMessage("courseResourcesExhaustion.subject", null, Locale.of(language));

        if (isRg) {
            templateModel.put("resourceGroup", resourceName);
            mailHelper.sendHtmlEmail(subject, emailTo, "courseResourcesExhaustionRg", templateModel, language);
        } else {
            templateModel.put("resourceGroupPool", resourceName);
            mailHelper.sendHtmlEmail(subject, emailTo, "courseResourcesExhaustionRgPool", templateModel, language);
        }
    }

    @PreAuthorize("permitAll()")
    public void sendReservationEndEmail(String emailTo,
                                        Reservation reservation,
                                        String timeZone,
                                        String language) {
        LocalDateTime endTime = OffsetDateTime.of(reservation.getEndTime(), ZoneOffset.UTC)
                .atZoneSameInstant(ZoneId.of(timeZone)).toLocalDateTime();

        String end = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String description = messageSource.getMessage("reservationEnd.generalDescription", null, Locale.of(language));
        Map<String, Object> templateModel = Map.of(
                "name", emailTo,
                "description", description.formatted(reservation.getNotificationTime()),
                "teamName", reservation.getTeam().getName(),
                "resourceGroupName", reservation.getResourceGroup().getName(),
                "scheduledEndTime", end
        );

        String subject = messageSource.getMessage("reservationEnd.subject", null, Locale.of(language));
        mailHelper.sendHtmlEmail(subject, emailTo, "reservationEnd", templateModel, language);
    }

    @Value("${executor.mail.urls.reservations}")
    private String reservationsBaseUrl;

    @PreAuthorize("permitAll()")
    public void sendReservationStartEmail(String emailTo,
                                          Reservation reservation,
                                          String timeZone,
                                          String language) {
        LocalDateTime endTime = OffsetDateTime.of(reservation.getEndTime(), ZoneOffset.UTC)
                .atZoneSameInstant(ZoneId.of(timeZone)).toLocalDateTime();

        String end = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String description = messageSource.getMessage("reservationEnd.generalDescription", null, Locale.of(language));
        Map<String, Object> templateModel = Map.of(
                "name", emailTo,
                "description", description.formatted(reservation.getNotificationTime()),
                "cancelUrl", reservationsBaseUrl + "/" + reservation.getId().toString(),
                "teamName", reservation.getTeam().getName(),
                "resourceGroupName", reservation.getResourceGroup().getName(),
                "scheduledEndTime", end
        );

        //TODO michal getUrl from props

        String subject = messageSource.getMessage("reservationStart.subject", null, Locale.of(language));
        mailHelper.sendHtmlEmail(subject, emailTo, "reservationStart", templateModel, language);
    }
}
