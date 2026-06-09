package devices.configuration.communication;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CommunicationService {

    private final HeartbeatInterval heartbeatInterval;
    private final Clock clock;

    public HeartbeatResponse handleBoot(String deviceId, BootNotification boot) {
        Duration interval = heartbeatInterval.heartbeatIntervalFor(boot);
        return new HeartbeatResponse(Instant.now(clock), (int) interval.toSeconds());
    }

    public record HeartbeatResponse(Instant currentTime, int intervalSeconds) {}
}
