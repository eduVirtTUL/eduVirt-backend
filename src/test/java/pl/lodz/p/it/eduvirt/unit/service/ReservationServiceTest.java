package pl.lodz.p.it.eduvirt.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.eduvirt.repository.eduvirt.*;
import pl.lodz.p.it.eduvirt.service.ReservationService;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ResourceGroupRepository resourceGroupRepository;

    @Mock
    private ResourceGroupPoolRepository resourceGroupPoolRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private ReservationService reservationService;

    /* Initialization */

    @BeforeEach
    public void prepareTestData() {

    }

    /* Tests */
}
