package pl.lodz.p.it.eduvirt.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.sdk4.services.HostService;
import org.ovirt.engine.sdk4.services.HostsService;
import org.ovirt.engine.sdk4.services.SystemService;
import org.ovirt.engine.sdk4.types.Host;
import pl.lodz.p.it.eduvirt.service.impl.OVirtHostServiceImpl;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;
import org.ovirt.engine.sdk4.Connection;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OVirtHostServiceTest {

    @Mock
    private ConnectionFactory connectionFactory;

    @InjectMocks
    private OVirtHostServiceImpl oVirtHostService;

    @Mock
    private Connection connection;

    @Mock
    private SystemService systemService;

    @Mock
    private HostsService hostsService;

    @Mock
    private HostService hostService;

    @Mock
    private HostService.GetRequest getRequest;

    @Mock
    private HostService.GetResponse getResponse;

    @Mock
    private Host host;

    /* Tests */

    /* FindHostById method tests */

    @Test
    public void Given_ExistingHostIdentifierIsPassed_When_FindHostById_Then_ReturnsFoundHostInfoAboutGivenHost() {
        UUID hostId = UUID.randomUUID();

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.hostsService()).thenReturn(hostsService);
        when(hostsService.hostService(Mockito.eq(hostId.toString()))).thenReturn(hostService);
        when(hostService.get()).thenReturn(getRequest);
        when(getRequest.send()).thenReturn(getResponse);
        when(getResponse.host()).thenReturn(host);

        Host foundHost = oVirtHostService.findHostById(hostId);

        assertNotNull(foundHost);
        assertEquals(foundHost, host);

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).hostsService();
        verify(hostsService, times(1)).hostService(Mockito.eq(hostId.toString()));
        verify(hostService, times(1)).get();
        verify(getRequest, times(1)).send();
        verify(getResponse, times(1)).host();
    }

    @Test
    public void Given_NonExistentHostIdentifierIsPassed_When_FindHostById_Then_ThrowsException() {
        UUID hostId = UUID.randomUUID();

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.hostsService()).thenReturn(hostsService);
        when(hostsService.hostService(Mockito.eq(hostId.toString()))).thenReturn(hostService);
        when(hostService.get()).thenReturn(getRequest);
        when(getRequest.send()).thenThrow(new org.ovirt.engine.sdk4.Error("Host not found"));

        assertThrows(RuntimeException.class, () -> oVirtHostService.findHostById(hostId));

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).hostsService();
        verify(hostsService, times(1)).hostService(Mockito.eq(hostId.toString()));
        verify(hostService, times(1)).get();
        verify(getRequest, times(1)).send();
    }
}
