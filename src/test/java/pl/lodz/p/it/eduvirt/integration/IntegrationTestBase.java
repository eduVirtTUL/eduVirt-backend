package pl.lodz.p.it.eduvirt.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles(profiles = {"test"})
public class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @LocalServerPort
    private int portNumber;

    protected ObjectMapper mapper;

    // Other constants

    protected final UUID nonExistentClusterId = UUID.randomUUID();
    protected final UUID existingClusterId = UUID.fromString("784cca54-f15d-43e7-b76c-f95a342fdf69");

    protected final UUID nonExistentMetricId = UUID.randomUUID();
    protected final UUID existingMetricId = UUID.fromString("21db3b91-39ff-4e21-b193-61318e6c362c");

    protected final UUID nonExistentMaintenanceIntervalId = UUID.randomUUID();
    protected final UUID existingMaintenanceIntervalId = UUID.fromString("fe14bd1c-59c4-428b-8f72-332a0688a488");

    protected final UUID nonExistentReservationId = UUID.randomUUID();
    protected final UUID existingReservationId = UUID.fromString("2831d827-8d96-40b7-9db8-9e24af2db1d2");

    protected final UUID nonExistentResourceGroupId = UUID.randomUUID();
    protected final UUID existingResourceGroupId = UUID.fromString("a1215a06-de2b-4eea-9152-513a96d7cc7d");

    protected final UUID nonExistentTeamId = UUID.randomUUID();
    protected final UUID existingTeamId = UUID.fromString("eedf635f-f2c4-4f62-9401-8cbbd00632f5");
}
