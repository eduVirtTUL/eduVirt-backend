package pl.lodz.p.it.eduvirt.unit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.lodz.p.it.eduvirt.aspect.exception.GeneralControllerExceptionResolver;
import pl.lodz.p.it.eduvirt.controller.ClusterMetricController;
import pl.lodz.p.it.eduvirt.mappers.ClusterMetricMapper;
import pl.lodz.p.it.eduvirt.service.ClusterMetricService;
import pl.lodz.p.it.eduvirt.service.impl.OVirtClusterServiceImpl;

@Import({
        ClusterMetricController.class,
        GeneralControllerExceptionResolver.class
})
@WebMvcTest(controllers = {ClusterMetricController.class}, useDefaultFilters = false)
public class ClusterMetricControllerTest {

    @MockitoBean
    private ClusterMetricService clusterMetricService;

    @MockitoBean
    private OVirtClusterServiceImpl oVirtClusterServiceImpl;

    /* Mappers */

    @MockitoBean
    private ClusterMetricMapper clusterMetricMapper;

    /* Initialization */

    @BeforeEach
    public void prepareTestData() {}

    /* Tests */

    /* CreateMetricValue method tests */

    /* GetAllMetricValues method tests */

    /* UpdateMetricValue method tests */

    /* DeleteMetric method tests */
}
