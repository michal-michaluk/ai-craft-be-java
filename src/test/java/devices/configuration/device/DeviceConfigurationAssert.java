package devices.configuration.device;

import org.assertj.core.api.Assertions;

import java.util.Optional;

import static devices.configuration.device.Violations.builder;
import static devices.configuration.device.Visibility.ForCustomer.INACCESSIBLE_AND_HIDDEN_ON_MAP;

public class DeviceConfigurationAssert {
    private final DeviceConfiguration device;

    private DeviceConfigurationAssert(DeviceConfiguration device) {
        this.device = device;
    }

    public static DeviceConfigurationAssert assertThat(Device actual) {
        return new DeviceConfigurationAssert(actual.toDeviceConfiguration());
    }

    public static DeviceConfigurationAssert assertThat(DeviceConfiguration actual) {
        return new DeviceConfigurationAssert(actual);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static DeviceConfigurationAssert assertThat(Optional<DeviceConfiguration> actual) {
        Assertions.assertThat(actual).isNotEmpty();
        return new DeviceConfigurationAssert(actual.get());
    }

    public DeviceConfigurationAssert hasSettings(Settings expected) {
        Assertions.assertThat(device.settings()).isEqualTo(expected);
        return this;
    }

    public DeviceConfigurationAssert hasSettings(Settings.SettingsBuilder expected) {
        Assertions.assertThat(device.settings()).isEqualTo(expected.build());
        return this;
    }

    public DeviceConfigurationAssert hasOwnership(Ownership expected) {
        Assertions.assertThat(device.ownership()).isEqualTo(expected);
        return this;
    }

    public DeviceConfigurationAssert hasLocation(Location expected) {
        Assertions.assertThat(device.location()).isEqualTo(expected);
        return this;
    }

    public DeviceConfigurationAssert hasOpeningHours(OpeningHours expected) {
        Assertions.assertThat(device.openingHours()).isEqualTo(expected);
        return this;
    }

    public DeviceConfigurationAssert hasNoViolations() {
        Assertions.assertThat(device.violations().isValid()).isTrue();
        return this;
    }

    public DeviceConfigurationAssert hasViolations(Violations.ViolationsBuilder violations) {
        Assertions.assertThat(device.violations())
                .isEqualTo(violations.build());
        return this;
    }

    public DeviceConfigurationAssert hasViolationsLikeNotConfiguredDevice() {
        hasViolations(builder()
                .operatorNotAssigned(true)
                .providerNotAssigned(true)
                .locationMissing(true)
                .showOnMapButMissingLocation(false)
                .showOnMapButNoPublicAccess(false)
        );
        return this;
    }

    public DeviceConfigurationAssert isNotVisible() {
        Assertions.assertThat(device.visibility())
                .isEqualTo(new Visibility(false, INACCESSIBLE_AND_HIDDEN_ON_MAP));
        return this;
    }
}
