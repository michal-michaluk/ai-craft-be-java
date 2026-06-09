package devices.configuration.intervals;

import java.time.Duration;
import java.util.regex.Pattern;

public sealed interface IntervalRule {

    Duration interval();

    boolean matches(String deviceId, String vendorName, String model);

    record DeviceIdRule(String deviceId, Duration interval) implements IntervalRule {
        public DeviceIdRule {
            if (deviceId == null || deviceId.isBlank())
                throw new IllegalArgumentException("deviceId must not be blank");
        }

        @Override
        public boolean matches(String deviceId, String vendorName, String model) {
            return this.deviceId.equals(deviceId);
        }
    }

    record ModelRule(String vendor, Pattern modelPattern, Duration interval) implements IntervalRule {
        public ModelRule {
            if (vendor == null || vendor.isBlank())
                throw new IllegalArgumentException("vendor must not be blank");
        }

        @Override
        public boolean matches(String deviceId, String vendorName, String model) {
            return this.vendor.equals(vendorName) && modelPattern.matcher(model).matches();
        }
    }
}
