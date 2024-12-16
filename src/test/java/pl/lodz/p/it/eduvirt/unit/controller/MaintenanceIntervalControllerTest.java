package pl.lodz.p.it.eduvirt.unit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.lodz.p.it.eduvirt.aspect.exception.GeneralControllerExceptionResolver;
import pl.lodz.p.it.eduvirt.aspect.exception.MaintenanceIntervalExceptionResolver;
import pl.lodz.p.it.eduvirt.controller.MaintenanceIntervalController;
import pl.lodz.p.it.eduvirt.mappers.MaintenanceIntervalMapper;
import pl.lodz.p.it.eduvirt.service.MaintenanceIntervalService;
import pl.lodz.p.it.eduvirt.service.OVirtClusterService;

@Import({
        MaintenanceIntervalController.class,
        MaintenanceIntervalExceptionResolver.class,
        GeneralControllerExceptionResolver.class
})
@WebMvcTest(controllers = {MaintenanceIntervalController.class}, useDefaultFilters = false)
public class MaintenanceIntervalControllerTest {

    @MockitoBean
    private MaintenanceIntervalService maintenanceIntervalService;

    @MockitoBean
    private OVirtClusterService clusterService;

    /* Mappers */

    @MockitoBean
    private MaintenanceIntervalMapper maintenanceIntervalMapper;

    /* Initialization */

    @BeforeEach
    public void prepareTestData() {}

    /* Tests */

    /* CreateNewClusterMaintenanceInterval method tests */

    /* CreateNewSystemMaintenanceInterval method tests */

    /* GetAllMaintenanceIntervals method tests */

    /* GetMaintenanceIntervalsWithinTimePeriod method tests */

    /* GetMaintenanceInterval method tests */

    /* FinishMaintenanceInterval method tests */
}
