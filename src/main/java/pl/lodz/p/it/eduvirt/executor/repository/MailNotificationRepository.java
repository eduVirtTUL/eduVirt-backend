package pl.lodz.p.it.eduvirt.executor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.executor.entity.mails.MailNotification;

import java.util.UUID;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface MailNotificationRepository extends JpaRepository<MailNotification, UUID> {
}
