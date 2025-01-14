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
import pl.lodz.p.it.eduvirt.controller.CourseController;
import pl.lodz.p.it.eduvirt.entity.AbstractEntity;
import pl.lodz.p.it.eduvirt.entity.Updatable;
import pl.lodz.p.it.eduvirt.mappers.*;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.*;

import java.lang.reflect.Field;

@Import({
        CourseController.class, GeneralControllerExceptionResolver.class,
        CourseMapperImpl.class, ResourceGroupMapperImpl.class,
        UserMapperImpl.class, RGPoolMapperImpl.class
})
@WebMvcTest(controllers = {CourseController.class}, useDefaultFilters = false)
public class CourseControllerTest {

    /* MockMVC */

    @Autowired
    private MockMvc mockMvc;

    /* Services */

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private ResourceGroupService resourceGroupService;

    @MockitoBean
    private ResourceGroupPoolService resourceGroupPoolService;

    @MockitoBean
    private TeamService teamService;

    @MockitoBean
    private CourseService courseService;

    /* Repositories */

    @MockitoBean
    private UserRepository userRepository;

    /* Mappers */

    @MockitoSpyBean
    private CourseMapper courseMapper;

    @MockitoSpyBean
    private RGPoolMapper rgPoolMapper;

    @MockitoSpyBean
    private ResourceGroupMapper resourceGroupMapper;

    @MockitoSpyBean
    private UserMapper userMapper;

    /* Data initialization */

    @BeforeEach
    public void setUp() throws Exception {
        Field id = AbstractEntity.class.getDeclaredField("id");
        Field version = Updatable.class.getDeclaredField("version");
    }

    /* Test methods */

    /* GetCoursesForStudent method tests */

    @Test
    public void Given__When_GetCoursesForStudent_Then_() {

    }

    /* FindResourcesAvailabilityForResourceGroup method tests */

    @Test
    public void Given__When_FindResourcesAvailabilityForResourceGroup_Then_() {

    }

    /* FindResourcesAvailabilityForResourceGroupPool method tests */

    @Test
    public void FindResourcesAvailabilityForResourceGroupPool() {

    }
}
