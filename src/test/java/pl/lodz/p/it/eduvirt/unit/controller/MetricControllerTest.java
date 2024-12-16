package pl.lodz.p.it.eduvirt.unit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.lodz.p.it.eduvirt.aspect.exception.GeneralControllerExceptionResolver;
import pl.lodz.p.it.eduvirt.aspect.exception.MetricControllerExceptionResolver;
import pl.lodz.p.it.eduvirt.controller.MetricController;
import pl.lodz.p.it.eduvirt.mappers.MetricMapper;
import pl.lodz.p.it.eduvirt.service.MetricService;

@Import({
        MetricController.class,
        MetricControllerExceptionResolver.class,
        GeneralControllerExceptionResolver.class
})
@WebMvcTest(controllers = {MetricController.class}, useDefaultFilters = false)
public class MetricControllerTest {

    @MockitoBean
    private MetricService metricService;

    /* Mappers */

    @MockitoBean
    private MetricMapper metricMapper;

    /* Initialization */

    @BeforeEach
    public void prepareTestData() {}

    /* Tests */

    /* CreateNewMetric method tests */

    /* GetAllMetrics method tests */

    /* DeleteMetric method tests */
}
