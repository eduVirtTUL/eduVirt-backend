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
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.CreateRGPoolDto;
import pl.lodz.p.it.eduvirt.dto.resource_group_pool.UpdateResourceGroupPoolDto;
import pl.lodz.p.it.eduvirt.util.RoleConstants;
import pl.lodz.p.it.eduvirt.utils.EnableTestcontainers;
import pl.lodz.p.it.eduvirt.utils.EntitiesConstants;

import java.util.UUID;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableTestcontainers
class ResourceGroupPoolControllerIT {
    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        webAppContextSetup(context, springSecurity());
    }

    @Test
    @WithMockUser(username = EntitiesConstants.TEACHER_ID, authorities = {RoleConstants.TEACHER})
    void Given_InputDataIsCorrect_When_CreateResourceGroupPool_ThenReturn200AndEntity() {
        CreateRGPoolDto data = CreateRGPoolDto.builder()
                .name("name")
                .courseId(UUID.fromString(EntitiesConstants.COURSE_ID))
                .maxRent(0)
                .gracePeriod(0)
                .description("description")
                .maxRentTime(0)
                .build();

        given()
                .body(data)
                .contentType(MediaType.APPLICATION_JSON)
                .post("/resource-group-pool")
                .then()
                .statusCode(200)
                .body("name", is("name"));
    }

    @Test
    @WithMockUser(username = EntitiesConstants.TEACHER_ID, authorities = {RoleConstants.TEACHER})
    void Given_InputDataIsCorrect_When_GetResourceGroupPool_ThenReturn200AndEntity() {
        given()
                .pathParam("id", EntitiesConstants.RESOURCE_GROUP_POOL_ID)
                .when()
                .get("/resource-group-pool/{id}")
                .then()
                .statusCode(200)
                .body("id", is(EntitiesConstants.RESOURCE_GROUP_POOL_ID));
    }

    @Test
    @WithMockUser(username = EntitiesConstants.ADMIN_ID, authorities = {RoleConstants.ADMINISTRATOR})
    void Given_InputDataIsCorrect_When_GetAll_ThenReturn200AndEntities() {
        given()
                .when()
                .get("/resource-group-pool")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @WithMockUser(username = EntitiesConstants.TEACHER_ID, authorities = {RoleConstants.TEACHER})
    void Given_InputDataIsCorrect_When_GetAllTeacher_ThenReturn200AndEntities() {
        given()
                .when()
                .get("/resource-group-pool/teacher")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @WithMockUser(username = EntitiesConstants.TEACHER_ID, authorities = {RoleConstants.TEACHER})
    void Given_InputDataIsCorrect_When_UpdateResourceGroupPool_ThenReturn200AndEntity() {
        //get resource group data and extract etag
        String etag = given()
                .pathParam("id", EntitiesConstants.RESOURCE_GROUP_POOL_ID)
                .when()
                .get("/resource-group-pool/{id}")
                .then()
                .extract()
                .header("ETag");

        etag = etag.substring(1, etag.length() - 1);

        UpdateResourceGroupPoolDto data = UpdateResourceGroupPoolDto.builder()
                .name("newName")
                .description("newDescription")
                .maxRent(10)
                .gracePeriod(10)
                .maxRentTime(10)
                .build();

        given()
                .pathParam("id", EntitiesConstants.RESOURCE_GROUP_POOL_ID)
                .body(data)
                .header("If-Match", etag)
                .contentType(MediaType.APPLICATION_JSON)
                .put("/resource-group-pool/{id}")
                .then()
                .statusCode(200)
                .body("name", is("newName"));
    }

    @Test
    @WithMockUser(username = EntitiesConstants.TEACHER_ID, authorities = {RoleConstants.TEACHER})
    void Given_InputDataIsCorrect_When_Delete_ThenReturn200() {
        given()
                .pathParam("id", EntitiesConstants.RESOURCE_GROUP_POOL_2_ID)
                .when()
                .delete("/resource-group-pool/{id}")
                .then()
                .statusCode(200);
    }
}
