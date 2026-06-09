package devices.configuration.intervals;

import devices.configuration.communication.BootNotification;
import devices.configuration.communication.HeartbeatInterval;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
class HeartbeatIntervalService implements HeartbeatInterval {
    private final IntervalRulesRepository repository;

    @Override
    public Duration heartbeatIntervalFor(BootNotification boot) {
        return repository.get()
                .evaluate(boot.deviceId(), boot.vendorName(), boot.model());
    }
}
