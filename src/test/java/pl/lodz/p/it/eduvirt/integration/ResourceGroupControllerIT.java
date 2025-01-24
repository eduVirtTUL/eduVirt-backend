package pl.lodz.p.it.eduvirt.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;
import pl.lodz.p.it.eduvirt.dto.course.CreateCourseDto;
import pl.lodz.p.it.eduvirt.entity.key.CourseType;
import pl.lodz.p.it.eduvirt.util.I18n;
import pl.lodz.p.it.eduvirt.util.RoleConstants;
import pl.lodz.p.it.eduvirt.utils.EnableTestcontainers;

import java.util.UUID;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableTestcontainers
class ResourceGroupControllerIT {
    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        webAppContextSetup(context, springSecurity());
    }

    @WithMockUser(username = "admin", authorities = {RoleConstants.ADMINISTRATOR})
    @Test
    void Given_CoursesDoNotExists_When_GetAll_Then_Return200AndEmptyList() {
        given()
                .when()
                .get("/course")
                .then()
                .statusCode(200)
                .body("items", hasSize(0));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {RoleConstants.ADMINISTRATOR})
    void Given_CourseDoesNotExist_When_GetCourse_Then_Return404() {
        given()
                .when()
                .get("/course/{courseId}", UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {RoleConstants.ADMINISTRATOR})
    void Given_TeacherDoNotExists_When_Create_Then_Return404AndUserNotFound() {
        CreateCourseDto createCourseDto = new CreateCourseDto(
                "name",
                "description",
                CourseType.SOLO,
                UUID.randomUUID(),
                "language",
                "test@test.com");

        given()
                .body(createCourseDto)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .post("/course")
                .then()
                .statusCode(404)
                .body("key", is(I18n.USER_NOT_FOUND));

    }
}
