package devices.configuration.device;

public record DeviceConfiguration(
        String deviceId,
        Ownership ownership,
        Location location,
        OpeningHours openingHours,
        Settings settings,
        Violations violations,
        Visibility visibility) {
}
