package pl.lodz.p.it.eduvirt.repository.eduvirt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.Reservation;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    //TODO michal: maybe add flag to reservation or check executor_task table to check which reservation was processing
    //TODO michal: optimization
    //TODO michal: change r.endTime to 'r.endTime - 5 minutes' for ex. -> starting reservations for a few seconds makes no sense..
    @Query("""
            SELECT DISTINCT r FROM Reservation r
            WHERE current_timestamp BETWEEN r.startTime AND r.endTime
            AND r.id NOT IN (SELECT et.reservation.id FROM ExecutorTask et WHERE et.type = 'POD_INIT' AND et.status != 'FAILED')
            """)
    List<Reservation> findReservationsToBegin();

    //TODO michal: r.endTime - 5 minutes -> due to the potential start of the next reservation immediately after this one
    @Query("""
            SELECT DISTINCT r FROM Reservation r
            WHERE current_timestamp >= r.endTime
            AND r.id NOT IN (SELECT et.reservation.id FROM ExecutorTask et WHERE et.type = 'POD_DESTRUCT' AND et.status != 'FAILED')
            """)
    List<Reservation> findReservationsToFinish();
}
