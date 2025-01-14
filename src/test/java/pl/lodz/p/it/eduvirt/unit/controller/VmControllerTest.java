package pl.lodz.p.it.eduvirt.unit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.lodz.p.it.eduvirt.aspect.exception.GeneralControllerExceptionResolver;
import pl.lodz.p.it.eduvirt.controller.VmController;
import pl.lodz.p.it.eduvirt.entity.AbstractEntity;
import pl.lodz.p.it.eduvirt.entity.Updatable;
import pl.lodz.p.it.eduvirt.mappers.EventMapper;
import pl.lodz.p.it.eduvirt.mappers.EventMapperImpl;
import pl.lodz.p.it.eduvirt.mappers.VmMapper;
import pl.lodz.p.it.eduvirt.mappers.VmMapperImpl;
import pl.lodz.p.it.eduvirt.service.OVirtClusterService;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;
import pl.lodz.p.it.eduvirt.service.OVirtVnicProfileService;

import java.lang.reflect.Field;

@Import({
        VmController.class, GeneralControllerExceptionResolver.class,
        VmMapperImpl.class, EventMapperImpl.class
})
@WebMvcTest(controllers = {VmController.class}, useDefaultFilters = false)
public class VmControllerTest {

    /* MockMVC */

    @Autowired
    private MockMvc mockMvc;

    /* Services */

    @MockitoBean
    private OVirtVmService oVirtVmService;

    @MockitoBean
    private OVirtClusterService oVirtClusterService;

    @MockitoBean
    private OVirtVnicProfileService oVirtVnicProfileService;

    /* Mappers */

    @MockitoSpyBean
    private VmMapper vmMapper;

    @MockitoSpyBean
    private EventMapper eventMapper;

    /* Data initialization */

    @BeforeEach
    public void setUp() throws Exception {
        Field id = AbstractEntity.class.getDeclaredField("id");
        Field version = Updatable.class.getDeclaredField("version");
    }

    /* Test methods */

    /* FindVmsForCluster method tests */

    @Test
    public void Given__When_FindVmsForCluster_Then_() {

    }

    /* FindEventsForVm methods tests */

    @Test
    public void Given__When_FindEventsForVm_Then_() {

    }
}
