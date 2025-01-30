package pl.lodz.p.it.eduvirt.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;
import pl.lodz.p.it.eduvirt.dto.resource_group.UpdateResourceGroupDto;
import pl.lodz.p.it.eduvirt.util.RoleConstants;
import pl.lodz.p.it.eduvirt.utils.EnableTestcontainers;
import pl.lodz.p.it.eduvirt.utils.EntitiesConstants;

import java.util.UUID;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static org.hamcrest.Matchers.*;
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

    @Test
    @WithMockUser(username = EntitiesConstants.ADMIN_ID, authorities = {RoleConstants.ADMINISTRATOR})
    void Given_ResourceGroupExists_When_GetById_ThenReturn200AndEntity() {
        given()
                .pathParam("id", EntitiesConstants.RESOURCE_GROUP_ID)
                .when()
                .get("/resource-group/{id}")
                .then()
                .statusCode(200)
                .header(HttpHeaders.ETAG, notNullValue())
                .body("id", is(EntitiesConstants.RESOURCE_GROUP_ID));
    }

    @Test
    @WithMockUser(username = EntitiesConstants.ADMIN_ID, authorities = {RoleConstants.ADMINISTRATOR})
    void Given_ResourceGroupDoNotExists_When_GetById_ThenReturn404() {
        given()
                .pathParam("id", UUID.randomUUID())
                .when()
                .get("/resource-group/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    @WithMockUser(username = EntitiesConstants.ADMIN_ID, authorities = {RoleConstants.ADMINISTRATOR})
    void Given_ResourceGroupsExists_When_GetAll_ThenReturn200AndEntities() {
        given()
                .when()
                .get("/resource-group")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @WithMockUser(username = EntitiesConstants.TEACHER_ID, authorities = {RoleConstants.TEACHER})
    void Given_InputDataIsCorrect_When_Update_ThenReturn200AndUpdatedEntity() {

        final var updateDto = new UpdateResourceGroupDto("newName", null, 10);

        String etag = given()
                .pathParam("id", EntitiesConstants.RESOURCE_GROUP_ID)
                .when()
                .get("/resource-group/{id}")
                .then()
                .statusCode(200)
                .extract()
                .header(HttpHeaders.ETAG);

        etag = etag.replace("\"", "");

        given()
                .pathParam("id", EntitiesConstants.RESOURCE_GROUP_ID)
                .body(updateDto)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.IF_MATCH, etag)
                .when()
                .put("/resource-group/{id}")
                .then()
                .statusCode(200)
                .body("name", is("newName"));
    }

    @Test
    @WithMockUser(username = EntitiesConstants.TEACHER_ID, authorities = {RoleConstants.TEACHER})
    void Given_ResourceGroupDoNotExists_When_Update_ThenReturn404() {
        final var updateDto = new UpdateResourceGroupDto("newName", null, 10);

        given()
                .pathParam("id", UUID.randomUUID())
                .body(updateDto)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.IF_MATCH, "0")
                .when()
                .put("/resource-group/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    @WithMockUser(username = EntitiesConstants.TEACHER_ID, authorities = {RoleConstants.TEACHER})
    void Given_ResourceGroupExists_When_Delete_ThenReturn204() {
        given()
                .pathParam("id", EntitiesConstants.RESOURCE_GROUP_2_ID)
                .when()
                .delete("/resource-group/{id}")
                .then()
                .statusCode(204);
    }

    @Test
    @WithMockUser(username = EntitiesConstants.TEACHER_ID, authorities = {RoleConstants.TEACHER})
    void Given_ResourceGroupWasAlreadyModified_When_Update_ThenReturn409() {
        final var updateDto = new UpdateResourceGroupDto("newName", null, 10);

        String etag = given()
                .pathParam("id", EntitiesConstants.RESOURCE_GROUP_ID)
                .when()
                .get("/resource-group/{id}")
                .then()
                .statusCode(200)
                .extract()
                .header(HttpHeaders.ETAG);

        etag = etag.replace("\"", "");

        given()
                .pathParam("id", EntitiesConstants.RESOURCE_GROUP_ID)
                .body(updateDto)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.IF_MATCH, etag)
                .when()
                .put("/resource-group/{id}")
                .then()
                .statusCode(200)
                .body("name", is("newName"));

        given()
                .pathParam("id", EntitiesConstants.RESOURCE_GROUP_ID)
                .body(updateDto)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.IF_MATCH, etag)
                .when()
                .put("/resource-group/{id}")
                .then()
                .statusCode(409);
    }

    @Test
    @WithMockUser(username = EntitiesConstants.TEACHER_ID, authorities = {RoleConstants.TEACHER})
    void Given_InputDataIsIncorrect_When_Update_ThenReturn400() {
        final var updateDto = new UpdateResourceGroupDto(null, null, 10);

        String etag = given()
                .pathParam("id", EntitiesConstants.RESOURCE_GROUP_ID)
                .when()
                .get("/resource-group/{id}")
                .then()
                .statusCode(200)
                .extract()
                .header(HttpHeaders.ETAG);

        etag = etag.replace("\"", "");

        given()
                .pathParam("id", EntitiesConstants.RESOURCE_GROUP_ID)
                .body(updateDto)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.IF_MATCH, etag)
                .when()
                .put("/resource-group/{id}")
                .then()
                .statusCode(400);
    }
}
