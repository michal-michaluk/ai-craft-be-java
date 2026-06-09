package devices.configuration.communication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunicationServiceTest {

    @Mock
    private HeartbeatInterval heartbeatInterval;
    @Mock
    private Clock clock;
    @InjectMocks
    private CommunicationService service;

    @Test
    void shouldReturnHeartbeatResponse() {
        when(clock.instant()).thenReturn(Instant.parse("2023-06-28T06:15:30.00Z"));
        when(heartbeatInterval.heartbeatIntervalFor(boot()))
                .thenReturn(Duration.ofSeconds(120));

        var response = service.handleBoot("device-id", boot());

        assertThat(response.currentTime()).isEqualTo(Instant.parse("2023-06-28T06:15:30.00Z"));
        assertThat(response.intervalSeconds()).isEqualTo(120);
    }

    private static BootNotification boot() {
        return new BootNotification("device-id", "iot16", "Garo", "CPF25 Family", "SN-001", "1.1");
    }
}
