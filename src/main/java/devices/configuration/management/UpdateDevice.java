package devices.configuration.management;

import java.util.Optional;

public record UpdateDevice(
        Optional<Ownership> ownership,
        Optional<Location> location,
        Optional<Settings.SettingsDiff> settings
) {

    public static UpdateDevice of(Ownership ownership, Location location) {
        return new UpdateDevice(Optional.of(ownership), Optional.of(location), Optional.empty());
    }

    public void apply(DeviceConfigurationEditor device) {
        ownership.ifPresent(device::assignTo);
        location.ifPresent(device::updateLocation);
        settings.ifPresent(device::updateSettings);
    }
}
