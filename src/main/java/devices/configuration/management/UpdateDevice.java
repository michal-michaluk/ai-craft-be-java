package devices.configuration.management;

import java.util.Optional;

public record UpdateDevice(
        Optional<Ownership> ownership,
        Optional<Location> location,
        Optional<Settings.SettingsDiff> settings
) {

    public void apply(DeviceConfigurationEditor device) {
        ownership.ifPresent(device::assignTo);
        location.ifPresent(device::updateLocation);
        settings.ifPresent(device::updateSettings);
    }
}
