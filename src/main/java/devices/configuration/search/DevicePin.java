package devices.configuration.search;

import devices.configuration.communication.DeviceStatuses;
import devices.configuration.device.DeviceConfiguration;
import devices.configuration.device.Location;

import java.util.List;
import java.util.Optional;

record DevicePin(
        String deviceId,
        Location.Coordinates coordinates,
        List<Status> statuses) {

    enum Status {AVAILABLE, CHARGING, FAULTED}

    static DevicePin ofNullable(DeviceConfiguration details, DeviceStatuses statuses) {
        if (details == null || details.location() == null) {
            return null;
        }
        return new DevicePin(
                details.deviceId(),
                details.location().coordinates(),
                Optional.ofNullable(statuses)
                        .map(s -> s.map(DevicePin::normalised))
                        .orElse(List.of(Status.FAULTED))
        );
    }

    private static Status normalised(String raw) {
        return switch (raw) {
            case "Available" -> Status.AVAILABLE;
            case "Faulted" -> Status.FAULTED;
            default -> Status.CHARGING;
        };
    }
}
