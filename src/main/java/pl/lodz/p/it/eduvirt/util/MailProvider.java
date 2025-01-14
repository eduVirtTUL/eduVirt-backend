package pl.lodz.p.it.eduvirt.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.Reservation;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MailProvider {

    private final MailHelper mailHelper;
    private final ResourceBundleMessageSource messageSource;

    /* Mail sending methods */

    @PreAuthorize("permitAll()")
    public void sendHtmlTestMessage(String firstName,
                                    String lastName,
                                    String emailTo,
                                    String timeZone,
                                    String language) {
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
    public void sendReservationRemovalEmail(String firstName,
                                            String lastName,
                                            String emailTo,
                                            Reservation reservation,
                                            String timeZone,
                                            String language) {
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
}
