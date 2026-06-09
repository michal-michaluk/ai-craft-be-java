package devices.configuration.communication;

import java.time.Duration;

public interface HeartbeatInterval {
    Duration heartbeatIntervalFor(BootNotification boot);
}
