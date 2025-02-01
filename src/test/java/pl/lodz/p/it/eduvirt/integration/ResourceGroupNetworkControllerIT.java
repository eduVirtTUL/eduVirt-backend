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
import pl.lodz.p.it.eduvirt.dto.resource_group_network.CreateResourceGroupNetworkDto;
import pl.lodz.p.it.eduvirt.util.RoleConstants;
import pl.lodz.p.it.eduvirt.utils.EnableTestcontainers;
import pl.lodz.p.it.eduvirt.utils.EntitiesConstants;

import java.util.UUID;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.webAppContextSetup;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableTestcontainers
class ResourceGroupNetworkControllerIT {
    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        webAppContextSetup(context, springSecurity());
    }

    @Test
    @WithMockUser(username = EntitiesConstants.TEACHER_ID, authorities = {RoleConstants.TEACHER})
    void Given_InputDataIsCorrect_When_addResourceGroupNetwork_ThenReturn200AndEntity() {
        String etag = given()
                .pathParam("rgId", EntitiesConstants.RESOURCE_GROUP_ID)
                .when()
                .get("/resource-group/{rgId}")
                .then()
                .extract()
                .header("ETag");

        etag = etag.substring(1, etag.length() - 1);

        final var createDto = new CreateResourceGroupNetworkDto("networkNameCreateNewNetwork");

        given()
                .pathParam("rgId", EntitiesConstants.RESOURCE_GROUP_ID)
                .header("If-Match", etag)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createDto)
                .when()
                .post("/resource-group/{rgId}/network")
                .then()
                .statusCode(200)
                .body("name", is("networkNameCreateNewNetwork"))
                .body("id", notNullValue());
    }

    @Test
    @WithMockUser(username = EntitiesConstants.TEACHER_ID, authorities = {RoleConstants.TEACHER})
    void Given_InputDataIsCorrect_When_get_ThenReturn200AndEntity() {
        given()
                .pathParam("rgId", EntitiesConstants.RESOURCE_GROUP_ID)
                .when()
                .get("/resource-group/{rgId}/network")
                .then()
                .statusCode(200);
    }

    @Test
    @WithMockUser(username = EntitiesConstants.TEACHER_ID, authorities = {RoleConstants.TEACHER})
    void Given_EtagIsInvalid_When_addResourceGroupNetwork_ThenReturn412() {
        final var createDto = new CreateResourceGroupNetworkDto("networkName");

        given()
                .pathParam("rgId", EntitiesConstants.RESOURCE_GROUP_ID)
                .header("If-Match", "invalidEtag")
                .contentType(MediaType.APPLICATION_JSON)
                .body(createDto)
                .when()
                .post("/resource-group/{rgId}/network")
                .then()
                .statusCode(412);
    }

    @Test
    @WithMockUser(username = EntitiesConstants.TEACHER_ID, authorities = {RoleConstants.TEACHER})
    void Given_ResourceGroupWasUpdated_When_addResourceGroupNetwork_ThenReturn409() {
        String etag = given()
                .pathParam("rgId", EntitiesConstants.RESOURCE_GROUP_ID)
                .when()
                .get("/resource-group/{rgId}")
                .then()
                .extract()
                .header("ETag");

        etag = etag.substring(1, etag.length() - 1);

        final var createDto = new CreateResourceGroupNetworkDto("networkName");

        given()
                .pathParam("rgId", EntitiesConstants.RESOURCE_GROUP_ID)
                .header("If-Match", etag)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createDto)
                .when()
                .post("/resource-group/{rgId}/network")
                .then()
                .statusCode(200)
                .body("name", is("networkName"))
                .body("id", notNullValue());

        final var createDto2 = new CreateResourceGroupNetworkDto("networkName2");

        given()
                .pathParam("rgId", EntitiesConstants.RESOURCE_GROUP_ID)
                .header("If-Match", etag)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createDto2)
                .when()
                .post("/resource-group/{rgId}/network")
                .then()
                .statusCode(409);
    }

    @Test
    @WithMockUser(username = EntitiesConstants.TEACHER_ID, authorities = {RoleConstants.TEACHER})
    void Given_InputDataIsIncorrect_When_addResourceGroupNetwork_ThenReturn400() {
        final var createDto = new CreateResourceGroupNetworkDto(null);

        given()
                .pathParam("rgId", EntitiesConstants.RESOURCE_GROUP_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createDto)
                .when()
                .post("/resource-group/{rgId}/network")
                .then()
                .statusCode(400);
    }

    @Test
    @WithMockUser(username = EntitiesConstants.TEACHER_ID, authorities = {RoleConstants.TEACHER})
    void Given_ResourceGroupDoNotExists_When_addResourceGroupNetwork_ThenReturn404() {
        final var createDto = new CreateResourceGroupNetworkDto("networkName");

        given()
                .pathParam("rgId", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .body(createDto)
                .header("If-Match", "1")
                .when()
                .post("/resource-group/{rgId}/network")
                .then()
                .statusCode(404);
    }

    @Test
    @WithMockUser(username = EntitiesConstants.TEACHER_ID, authorities = {RoleConstants.TEACHER})
    void Given_InputDataIsCorrect_When_deleteNetwork_ThenReturn200() {
        String etag = given()
                .pathParam("rgId", EntitiesConstants.ResourceGroup1.ID)
                .when()
                .get("/resource-group/{rgId}")
                .then()
                .extract()
                .header("ETag");

        etag = etag.substring(1, etag.length() - 1);

        given()
                .pathParam("rgId", EntitiesConstants.RESOURCE_GROUP_ID)
                .pathParam("id", EntitiesConstants.ResourceGroup1.NETWORK_ID)
                .header("If-Match", etag)
                .when()
                .delete("/resource-group/{rgId}/network/{id}")
                .then()
                .statusCode(200);
    }

    @Test
    @WithMockUser(username = EntitiesConstants.TEACHER_ID, authorities = {RoleConstants.TEACHER})
    void Given_ResourceGroupNetworkDoNotExists_When_deleteNetwork_ThenReturn200() {
        String etag = given()
                .pathParam("rgId", EntitiesConstants.RESOURCE_GROUP_ID)
                .when()
                .get("/resource-group/{rgId}")
                .then()
                .extract()
                .header("ETag");

        etag = etag.substring(1, etag.length() - 1);

        given()
                .pathParam("rgId", EntitiesConstants.RESOURCE_GROUP_ID)
                .pathParam("id", UUID.randomUUID())
                .header("If-Match", etag)
                .when()
                .delete("/resource-group/{rgId}/network/{id}")
                .then()
                .statusCode(200);
    }
}
