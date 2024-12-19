package pl.lodz.p.it.eduvirt.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import pl.lodz.p.it.eduvirt.dto.EventGeneralDTO;
import pl.lodz.p.it.eduvirt.dto.NetworkDto;
import pl.lodz.p.it.eduvirt.dto.cluster.ClusterDetailsDto;
import pl.lodz.p.it.eduvirt.dto.cluster.ClusterGeneralDto;
import pl.lodz.p.it.eduvirt.dto.host.HostDto;
import pl.lodz.p.it.eduvirt.dto.vm.VmGeneralDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClusterControllerIT extends IntegrationTestBase {

    /**
     * TEST REQUIREMENTS:
     *      That test requires some cluster to exist in the oVirt database, since its identifier will be
     *      used to find the cluster info.
     */
    // @Test
    public void Given_ExistingClusterIdIsPassed_When_FindClusterById_Then_ReturnsFoundCluster() throws Exception {
        MvcResult result = mockMvc.perform(get("/cluster/{clusterId}", existingClusterId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        ClusterDetailsDto clusterDetails = mapper.readValue(json, ClusterDetailsDto.class);

        assertNotNull(clusterDetails);
        assertNotNull(clusterDetails.id());
        assertNotNull(clusterDetails.name());
    }

    /**
     * TEST REQUIREMENTS:
     *      That test does not require any cluster to exist in the oVirt database, since random id will be used
     *      to find certain cluster.
     */
    // @Test
    public void Given_NonExistentClusterIdIsPassed_When_FindClusterById_Then_Returns404NotFound() throws Exception {
        mockMvc.perform(get("/cluster/{clusterId}", nonExistentClusterId))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST REQUIREMENTS:
     *      Some sample clusters existence in the oVirt DB is required, at least two of them would be
     *      sufficient.
     */
    // @Test
    public void Given_SomeClustersExistsInTheDatabase_When_FindAllClusters_Then_ReturnsAllFoundClusters() throws Exception {
        MvcResult result = mockMvc.perform(get("/cluster"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<ClusterGeneralDto> foundClusters = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(foundClusters);
        assertFalse(foundClusters.isEmpty());
        assertEquals(foundClusters.size(), 2);

        ClusterGeneralDto firstCluster = foundClusters.getFirst();
        assertNotNull(firstCluster);

        ClusterGeneralDto secondCluster = foundClusters.getLast();
        assertNotNull(secondCluster);
    }

    /**
     * TEST REQUIREMENTS:
     *      Sample cluster existence in the oVirt DB is required. Besides, it does also require to have some
     *      hosts inside it, since that endpoint tests it.
     */
    // @Test
    public void Given_ExistingClusterIdentifierIsPassed_When_FindHostInfoByClusterId_Then_ReturnsHostInfoOfGivenCluster() throws Exception {
        MvcResult result = mockMvc.perform(get("/cluster/{clusterId}/hosts", existingClusterId))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<HostDto> foundHosts = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(foundHosts);
        assertFalse(foundHosts.isEmpty());
        assertEquals(foundHosts.size(), 3);

        HostDto firstHost = foundHosts.getFirst();
        assertNotNull(firstHost);

        HostDto secondHost = foundHosts.getLast();
        assertNotNull(secondHost);
    }

    /**
     * TEST REQUIREMENTS:
     *      For the test below, no data is required in the eduVirt / oVirt DBs, since it uses randomly generated identifier
     *      to check what happens if the cluster could not be found.
     */
    // @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_FindHostInfoByClusterId_Then_Returns400BadRequest() throws Exception {
        mockMvc.perform(get("/cluster/{clusterId}/hosts", nonExistentClusterId))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST REQUIREMENTS:
     *      That test does require some cluster to exist along with certain number of virtual machines located on
     *      its hosts (which should be at least 2 [in order to test it properly]).
     */
    // @Test
    public void Given_ExistingClusterIdentifierIsPassed_When_FindVirtualMachinesByClusterId_Then_ReturnsFoundVirtualMachines() throws Exception {
        MvcResult result = mockMvc.perform(get("/clusters/{clusterId}/vms", existingClusterId))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<VmGeneralDto> foundVms = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(foundVms);
        assertFalse(foundVms.isEmpty());
        assertEquals(foundVms.size(), 2);

        VmGeneralDto firstVm = foundVms.getFirst();
        assertNotNull(firstVm);

        VmGeneralDto secondVm = foundVms.getLast();
        assertNotNull(secondVm);
    }

    /**
     * TEST REQUIREMENTS:
     *      That test does not require any cluster to exist in the oVirt DB, since it is using randomly generated
     *      cluster identifier to check what happens if the cluster could not be found.
     */
    // @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_FindVirtualMachinesByClusterId_Then_Returns400BadRequest() throws Exception {
        mockMvc.perform(get("/clusters/{clusterId}/vms", nonExistentClusterId))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST REQUIREMENTS:
     *      That test does require some sample cluster to exist in the oVirt DB along with certain number of networks
     *      that are attached to that cluster (there should be at least 2 of them in order to test it properly).
     */
    // @Test
    public void Given_ExistingClusterIdentifierIsPassed_When_FindNetworksByClusterId_Then_ReturnsFoundNetworks() throws Exception {
        MvcResult result = mockMvc.perform(get("/clusters/{clusterId}/networks", existingClusterId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        
        String json = result.getResponse().getContentAsString();
        List<NetworkDto> foundNetworks = mapper.readValue(json, new TypeReference<>() {});
        
        assertNotNull(foundNetworks);
        assertFalse(foundNetworks.isEmpty());
        assertEquals(foundNetworks.size(), 2);

        NetworkDto firstNetwork = foundNetworks.getFirst();
        assertNotNull(firstNetwork);

        NetworkDto secondNetwork = foundNetworks.getLast();
        assertNotNull(secondNetwork);
    }

    /**
     * TEST REQUIREMENTS:
     *      That test does not require any cluster to exist in the oVirt DB, since it is using randomly generated
     *      cluster identifier to check what happens if the cluster could not be found.
     */
    // @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_FindNetworksByClusterId_Then_Returns400BadRequest() throws Exception {
        mockMvc.perform(get("/clusters/{clusterId}/networks", nonExistentClusterId))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * TEST REQUIREMENTS:
     *      That test does require some sample cluster to exist in the oVirt DB. There should also be some
     *      events related to cluster (could be VM startup event, or anything related, which creates event
     *      object in the event tab in oVirt view of cluster).
     */
    // @Test
    public void Given_ExistingClusterIdentifierIsPassed_When_FindEventsByClusterId_Then_ReturnsFoundEvents() throws Exception {
        MvcResult result = mockMvc.perform(get("/clusters/{clusterId}/events", existingClusterId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        
        String json = result.getResponse().getContentAsString();
        List<EventGeneralDTO> foundEvents = mapper.readValue(json, new TypeReference<>() {});
        
        assertNotNull(foundEvents);
        assertFalse(foundEvents.isEmpty());
        assertEquals(foundEvents.size(), 2);

        EventGeneralDTO firstEvent = foundEvents.getFirst();
        assertNotNull(firstEvent);

        EventGeneralDTO secondEvent = foundEvents.getLast();
        assertNotNull(secondEvent);
    }

    /**
     * TEST REQUIREMENTS:
     *      That test does not require any cluster to exist in the oVirt DB, since it is using randomly generated
     *      cluster identifier to check what happens if the cluster could not be found.
     */
    // @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_FindEventsByClusterId_Then_Returns400BadRequest() throws Exception {
        mockMvc.perform(get("/clusters/{clusterId}/events", nonExistentClusterId))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
