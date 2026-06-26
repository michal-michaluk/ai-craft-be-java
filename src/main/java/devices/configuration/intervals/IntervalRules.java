package devices.configuration.intervals;

import devices.configuration.communication.BootNotification;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

record IntervalRules(Duration defaultInterval, List<DeviceRule> deviceRules, List<ModelRule> modelRules) {

    Duration calculateFor(BootNotification boot) {
        for (var rule : deviceRules) {
            if (rule.matches(boot)) return rule.interval();
        }
        for (var rule : modelRules) {
            if (rule.matches(boot)) return rule.interval();
        }
        return defaultInterval;
    }

    sealed interface Rule permits DeviceRule, ModelRule {
        boolean matches(BootNotification boot);
        Duration interval();
    }

    record DeviceRule(List<String> deviceIds, Duration interval) implements Rule {
        @Override
        public boolean matches(BootNotification boot) {
            return deviceIds.contains(boot.deviceId());
        }
    }

    record ModelRule(String vendor, String modelRegex, Duration interval) implements Rule {
        @Override
        public boolean matches(BootNotification boot) {
            if (boot.vendor() == null || boot.model() == null) return false;
            return vendor.equals(boot.vendor()) &&
                    Pattern.matches(modelRegex, boot.model());
        }
    }
}
