package devices.configuration.device;

import lombok.Builder;

import jakarta.validation.Valid;

@Builder
public record UpdateDevice(
        @Valid Location location,
        @Valid OpeningHours openingHours,
        @Valid Settings settings,
        @Valid Ownership ownership) {

    public static UpdateDevice use(Ownership ownership, Location location) {
        return builder()
                .location(location)
                .ownership(ownership)
                .build();
    }

    public void apply(Device device) {
        if (location != null) {
            device.updateLocation(location);
        }
        if (openingHours != null) {
            device.updateOpeningHours(openingHours);
        }
        if (settings != null) {
            device.updateSettings(settings);
        }
        if (ownership != null) {
            device.assignTo(ownership);
        }
    }
}
