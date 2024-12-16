package pl.lodz.p.it.eduvirt.unit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.lodz.p.it.eduvirt.aspect.exception.GeneralControllerExceptionResolver;
import pl.lodz.p.it.eduvirt.controller.ReservationController;
import pl.lodz.p.it.eduvirt.mappers.ReservationMapper;
import pl.lodz.p.it.eduvirt.service.ReservationService;

@Import({
        ReservationController.class,
        GeneralControllerExceptionResolver.class
})
@WebMvcTest(controllers = {ReservationController.class}, useDefaultFilters = false)
public class ReservationControllerTest {

    @MockitoBean
    private ReservationService reservationService;

    /* Mappers */

    @MockitoBean
    private ReservationMapper reservationMapper;

    /* Initialization */

    @BeforeEach
    public void prepareTestData() {}

    /* Test */

    /* CreateNewReservation method tests */

    /* GetReservationDetails method tests */

    /* GetReservationsForGivenPeriod method tests */

    /* GetActiveReservations method tests */

    /* GetHistoricReservations method tests */

    /* FinishReservation method tests */
}
