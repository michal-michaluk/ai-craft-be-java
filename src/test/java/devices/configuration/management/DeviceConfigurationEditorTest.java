package devices.configuration.management;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceConfigurationEditorTest {

    @Test
    void shouldAssignOwnershipAndEmitEvent() {
        // Given
        DeviceConfigurationEditor device = DeviceConfigurationEditor.createNewDevice("device-1");

        // When
        device.assignTo(Ownership.of("operator1", "provider1"));

        // Then
        assertThat(device.toDeviceConfiguration().ownership())
                .isEqualTo(Ownership.of("operator1", "provider1"));
        assertThat(device.events).hasSize(1);
        assertThat(device.events.get(0)).isInstanceOf(DomainEvent.OwnershipUpdated.class);
    }

    @Test
    void shouldResetToDefaultsWhenUnassigned() {
        // Given
        DeviceConfigurationEditor device = DeviceConfigurationEditor.createNewDevice("device-1");
        device.assignTo(Ownership.of("operator1", "provider1"));
        device.updateSettings(Settings.SettingsDiff.builder().autoStart(true).remoteControl(true).billing(true).reimbursement(true).showOnMap(true).publicAccess(true).build());
        device.events.clear();

        // When
        device.assignTo(Ownership.unowned());

        // Then
        DeviceConfiguration config = device.toDeviceConfiguration();
        assertThat(config.ownership()).isEqualTo(Ownership.unowned());
        assertThat(config.settings()).isEqualTo(Settings.defaultSettings());
        assertThat(device.events).hasSizeGreaterThan(1);
    }

    @Test
    void shouldUpdateLocationAndEmitEvent() {
        // Given
        DeviceConfigurationEditor device = DeviceConfigurationEditor.createNewDevice("device-1");
        Location.Coordinates coords = new Location.Coordinates(10.0, 20.0);
        Location location = new Location("street", "1", "city", "123", "PL", coords);

        // When
        device.updateLocation(location);

        // Then
        assertThat(device.toDeviceConfiguration().location()).isEqualTo(location);
        assertThat(device.events).hasSize(1);
        assertThat(device.events.get(0)).isInstanceOf(DomainEvent.LocationUpdated.class);
    }

    @Test
    void shouldUpdateOpeningHoursAndEmitEvent() {
        // Given
        DeviceConfigurationEditor device = DeviceConfigurationEditor.createNewDevice("device-1");
        OpeningHours hours = new OpeningHours(false);

        // When
        device.updateOpeningHours(hours);

        // Then
        assertThat(device.toDeviceConfiguration().openingHours()).isEqualTo(hours);
        assertThat(device.events).hasSize(1);
        assertThat(device.events.get(0)).isInstanceOf(DomainEvent.OpeningHoursUpdated.class);
    }

    @Test
    void shouldUpdateSettingsAndEmitEvent() {
        // Given
        DeviceConfigurationEditor device = DeviceConfigurationEditor.createNewDevice("device-1");
        Settings.SettingsDiff diff = Settings.SettingsDiff.builder().autoStart(true).build();
        Settings expectedSettings = new Settings(true, false, false, false, false, false);

        // When
        device.updateSettings(diff);

        // Then
        assertThat(device.toDeviceConfiguration().settings()).isEqualTo(expectedSettings);
        assertThat(device.events).hasSize(1);
        assertThat(device.events.get(0)).isInstanceOf(DomainEvent.SettingsUpdated.class);
    }

    @Test
    void shouldSetViolationWhenSettingShowOnMapWithoutLocation() {
        // Given
        DeviceConfigurationEditor device = DeviceConfigurationEditor.createNewDevice("device-1");
        Settings.SettingsDiff diff = Settings.SettingsDiff.builder().showOnMap(true).publicAccess(true).build();

        // When
        device.updateSettings(diff);

        // Then
        DeviceConfiguration config = device.toDeviceConfiguration();
        assertThat(config.violations().showOnMapButMissingLocation()).isTrue();
        assertThat(config.violations().showOnMapButNoPublicAccess()).isFalse();
    }

    @Test
    void shouldSetViolationWhenSettingShowOnMapWithoutPublicAccess() {
        // Given
        DeviceConfigurationEditor device = DeviceConfigurationEditor.createNewDevice("device-1");
        Location.Coordinates coords = new Location.Coordinates(10.0, 20.0);
        Location location = new Location("street", "1", "city", "123", "PL", coords);
        device.updateLocation(location);
        Settings.SettingsDiff diff = Settings.SettingsDiff.builder().showOnMap(true).publicAccess(false).build();

        // When
        device.updateSettings(diff);

        // Then
        DeviceConfiguration config = device.toDeviceConfiguration();
        assertThat(config.violations().showOnMapButMissingLocation()).isFalse();
        assertThat(config.violations().showOnMapButNoPublicAccess()).isTrue();
    }

    @Test
    void shouldAllowSettingShowOnMapWithLocationAndPublicAccess() {
        // Given
        DeviceConfigurationEditor device = DeviceConfigurationEditor.createNewDevice("device-1");
        Location.Coordinates coords = new Location.Coordinates(10.0, 20.0);
        Location location = new Location("street", "1", "city", "123", "PL", coords);
        device.updateLocation(location);
        Settings.SettingsDiff diff = Settings.SettingsDiff.builder().showOnMap(true).publicAccess(true).build();
        Settings expectedSettings = new Settings(false, false, false, false, true, true);

        // When
        device.updateSettings(diff);

        // Then
        DeviceConfiguration config = device.toDeviceConfiguration();
        assertThat(config.settings()).isEqualTo(expectedSettings);
        assertThat(config.violations().showOnMapButMissingLocation()).isFalse();
        assertThat(config.violations().showOnMapButNoPublicAccess()).isFalse();
    }

    @Test
    void shouldCheckViolationsCorrectly() {
        // Given
        DeviceConfigurationEditor device = DeviceConfigurationEditor.createNewDevice("device-1");
        // Default is unowned, no location, so violations: operatorNotAssigned=true, providerNotAssigned=true, locationMissing=true

        // When
        DeviceConfiguration config = device.toDeviceConfiguration();

        // Then
        assertThat(config.violations().operatorNotAssigned()).isTrue();
        assertThat(config.violations().providerNotAssigned()).isTrue();
        assertThat(config.violations().locationMissing()).isTrue();
        assertThat(config.violations().showOnMapButMissingLocation()).isFalse();
        assertThat(config.violations().showOnMapButNoPublicAccess()).isFalse();
        assertThat(config.violations().isValid()).isFalse();
    }

    @Test
    void shouldHaveNoViolationsWhenOwnedWithLocation() {
        // Given
        DeviceConfigurationEditor device = DeviceConfigurationEditor.createNewDevice("device-1");
        device.assignTo(Ownership.of("op", "prov"));
        Location.Coordinates coords = new Location.Coordinates(10.0, 20.0);
        Location location = new Location("street", "1", "city", "123", "PL", coords);
        device.updateLocation(location);

        // When
        DeviceConfiguration config = device.toDeviceConfiguration();

        // Then
        assertThat(config.violations().isValid()).isTrue();
    }

    @Test
    void shouldCalculateVisibilityCorrectly() {
        // Given
        DeviceConfigurationEditor device = DeviceConfigurationEditor.createNewDevice("device-1");
        device.assignTo(Ownership.of("op", "prov"));
        Location.Coordinates coords = new Location.Coordinates(10.0, 20.0);
        Location location = new Location("street", "1", "city", "123", "PL", coords);
        device.updateLocation(location);
        device.updateSettings(new Settings.SettingsDiff(null, null, null, null, true, true)); // showOnMap=true, publicAccess=true

        // When
        DeviceConfiguration config = device.toDeviceConfiguration();

        // Then
        assertThat(config.visibility().isPublic()).isTrue();
        assertThat(config.visibility().isVisible()).isTrue();
    }
}
