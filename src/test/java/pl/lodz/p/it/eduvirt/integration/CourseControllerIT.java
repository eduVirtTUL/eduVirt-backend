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
import pl.lodz.p.it.eduvirt.dto.course.UpdateCourseDto;
import pl.lodz.p.it.eduvirt.entity.key.CourseType;
import pl.lodz.p.it.eduvirt.util.RoleConstants;
import pl.lodz.p.it.eduvirt.utils.EnableTestcontainers;
import pl.lodz.p.it.eduvirt.utils.EntitiesConstants;

import java.util.UUID;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableTestcontainers
class CourseControllerIT {
    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        webAppContextSetup(context, springSecurity());
    }

    @Test
    @WithMockUser(username = EntitiesConstants.ADMIN_ID, authorities = {RoleConstants.ADMINISTRATOR})
    void Given_InputDataIsCorrect_When_CreateCourse_ThenReturn200AndEntity() {

        CreateCourseDto data = CreateCourseDto.builder()
                .name("name")
                .clusterId(UUID.randomUUID())
                .description("description")
                .courseType(CourseType.SOLO)
                .externalLink("externalLink")
                .teacherEmail("teacher@internal.test")
                .build();

        given()
                .body(data)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .post("/course")
                .then()
                .statusCode(201);
    }

    @Test
    @WithMockUser(username = EntitiesConstants.ADMIN_ID, authorities = {RoleConstants.ADMINISTRATOR})
    void Given_InputDataIsCorrect_When_DeleteCourse_Then_Return200() {
        given()
                .pathParam("id", EntitiesConstants.COURSE_2_ID)
                .when()
                .delete("/course/{id}")
                .then()
                .statusCode(200);
    }

    @Test
    @WithMockUser(username = EntitiesConstants.ADMIN_ID, authorities = {RoleConstants.ADMINISTRATOR})
    void Given_InputDataIsCorrect_When_UpdateCourse_Then_Return200() {
        String etag = given()
                .pathParam("id", EntitiesConstants.COURSE_ID)
                .when()
                .get("/course/{id}")
                .then()
                .extract()
                .header("ETag");

        etag = etag.substring(1, etag.length() - 1);

        UpdateCourseDto data = UpdateCourseDto.builder()
                .name("newName")
                .description("newDescription")
                .externalLink("das")
                .build();

        given()
                .pathParam("id", EntitiesConstants.COURSE_ID)
                .header("If-Match", etag)
                .body(data)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .put("/course/{id}")
                .then()
                .statusCode(200);

    }
}
