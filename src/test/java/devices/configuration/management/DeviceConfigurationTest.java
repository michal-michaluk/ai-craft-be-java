package devices.configuration.management;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceConfigurationTest {

    @Test
    void shouldCreateDeviceConfiguration() {
        String deviceId = "device1";
        Ownership ownership = Ownership.unowned();
        Location location = null;
        OpeningHours hours = OpeningHours.alwaysOpened();
        Settings settings = Settings.defaultSettings();
        Violations violations = Violations.builder().build();
        Visibility visibility = Visibility.basedOn(false, false);

        DeviceConfiguration config = new DeviceConfiguration(deviceId, ownership, location, hours, settings, violations, visibility);

        assertThat(config.deviceId()).isEqualTo(deviceId);
        assertThat(config.ownership()).isEqualTo(ownership);
        assertThat(config.location()).isNull();
        assertThat(config.openingHours()).isEqualTo(hours);
        assertThat(config.settings()).isEqualTo(settings);
        assertThat(config.violations()).isEqualTo(violations);
        assertThat(config.visibility()).isEqualTo(visibility);
    }

    @Test
    void shouldUseValueEquality() {
        DeviceConfiguration config1 = new DeviceConfiguration("id", Ownership.unowned(), null, OpeningHours.alwaysOpened(), Settings.defaultSettings(), Violations.builder().build(), Visibility.basedOn(false, false));
        DeviceConfiguration config2 = new DeviceConfiguration("id", Ownership.unowned(), null, OpeningHours.alwaysOpened(), Settings.defaultSettings(), Violations.builder().build(), Visibility.basedOn(false, false));
        assertThat(config1).isEqualTo(config2);
    }
}