package devices.configuration.intervals;

import java.time.Duration;
import java.util.List;

public record IntervalRules(
        List<IntervalRule.DeviceIdRule> deviceIdRules,
        List<IntervalRule.ModelRule> modelRules,
        Duration defaultInterval
) {
    public IntervalRules {
        deviceIdRules = List.copyOf(deviceIdRules);
        modelRules = List.copyOf(modelRules);
    }

    public static IntervalRules withDefaults() {
        return new IntervalRules(List.of(), List.of(), Duration.ofSeconds(1800));
    }

    public Duration evaluate(String deviceId, String vendorName, String model) {
        return deviceIdRules.stream()
                .filter(r -> r.matches(deviceId, vendorName, model))
                .map(IntervalRule::interval)
                .findFirst()
                .orElseGet(() -> modelRules.stream()
                        .filter(r -> r.matches(deviceId, vendorName, model))
                        .map(IntervalRule::interval)
                        .findFirst()
                        .orElse(defaultInterval));
    }
}
