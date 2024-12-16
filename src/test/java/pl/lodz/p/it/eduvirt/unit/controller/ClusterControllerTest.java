package pl.lodz.p.it.eduvirt.unit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.lodz.p.it.eduvirt.aspect.exception.GeneralControllerExceptionResolver;
import pl.lodz.p.it.eduvirt.controller.ClusterController;
import pl.lodz.p.it.eduvirt.mappers.*;
import pl.lodz.p.it.eduvirt.service.OVirtClusterService;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;

@Import({
        ClusterController.class,
        GeneralControllerExceptionResolver.class
})
@WebMvcTest(controllers = {ClusterController.class}, useDefaultFilters = false)
public class ClusterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OVirtClusterService clusterService;

    @MockitoBean
    private OVirtVmService vmService;

    /* Mappers */

    @MockitoBean
    private ClusterMapper clusterMapper;

    @MockitoBean
    private HostMapper hostMapper;

    @MockitoBean
    private NetworkMapper networkMapper;

    @MockitoBean
    private EventMapper eventMapper;

    @MockitoBean
    private VmMapper vmMapper;

    /* Initialization */

    @BeforeEach
    public void prepareTestData() {}

    /* Tests */

    /* FindClusterById method tests */

    /* FindAllClusters method tests */

    /* FindClusterResourcesAvailability method tests */

    /* FindHostInfoByClusterId method tests */

    /* FindVirtualMachinesByClusterId method tests */

    /* FindNetworksByClusterId method tests */

    /* FindEventsByClusterId method tests */
}
