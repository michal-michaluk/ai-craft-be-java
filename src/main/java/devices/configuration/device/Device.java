package devices.configuration.device;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static devices.configuration.device.DomainEvent.*;

@AllArgsConstructor
class Device {
    final String deviceId;
    final List<DomainEvent> events;
    private Ownership ownership;
    private Location location;
    private OpeningHours openingHours;
    private Settings settings;

    static Device newDevice(String deviceId) {
        return new Device(
                deviceId,
                new ArrayList<>(),
                Ownership.unowned(),
                null,
                OpeningHours.alwaysOpened(),
                Settings.defaultSettings()
        );
    }

    void resetToDefaults() {
        updateLocation(null);
        updateOpeningHours(OpeningHours.alwaysOpened());
        updateSettings(Settings.defaultSettings());
    }

    void assignTo(Ownership ownership) {
        Objects.requireNonNull(ownership);
        if (!Objects.equals(this.ownership, ownership)) {
            this.ownership = ownership;
            events.add(new OwnershipUpdated(deviceId, ownership));

            if (ownership.isUnowned()) {
                resetToDefaults();
            }
        }
    }

    void updateLocation(Location location) {
        if (!Objects.equals(this.location, location)) {
            this.location = location;
            events.add(new LocationUpdated(deviceId, location));
        }
    }

    void updateOpeningHours(OpeningHours openingHours) {
        Objects.requireNonNull(openingHours);
        if (!Objects.equals(this.openingHours, openingHours)) {
            this.openingHours = openingHours;
            events.add(new OpeningHoursUpdated(deviceId, openingHours));
        }
    }

    void updateSettings(Settings settings) {
        Objects.requireNonNull(settings);
        Settings merged = this.settings.merge(settings);
        if (!Objects.equals(this.settings, merged)) {
            this.settings = merged;
            events.add(new SettingsUpdated(deviceId, this.settings));
        }
    }

    private Violations checkViolations() {
        return Violations.builder()
                .operatorNotAssigned(ownership.operator() == null)
                .providerNotAssigned(ownership.provider() == null)
                .locationMissing(location == null)
                .showOnMapButMissingLocation(settings.isShowOnMap() && location == null)
                .showOnMapButNoPublicAccess(settings.isShowOnMap() && !settings.isPublicAccess())
                .build();
    }

    DeviceConfiguration toDeviceConfiguration() {
        Violations violations = checkViolations();
        Visibility visibility = Visibility.basedOn(
                violations.isValid() && settings.isPublicAccess(),
                settings.isShowOnMap()
        );
        return new DeviceConfiguration(
                deviceId,
                ownership,
                location,
                openingHours,
                settings,
                violations,
                visibility
        );
    }
}
