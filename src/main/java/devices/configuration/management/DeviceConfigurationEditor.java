package devices.configuration.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class DeviceConfigurationEditor {
    final String deviceId;
    final List<DomainEvent> events;
    private Ownership ownership;
    private Location location;
    private OpeningHours openingHours;
    private Settings settings;

    DeviceConfigurationEditor(String deviceId, List<DomainEvent> events, Ownership ownership, Location location, OpeningHours openingHours, Settings settings) {
        this.deviceId = deviceId;
        this.events = events;
        this.ownership = ownership;
        this.location = location;
        this.openingHours = openingHours;
        this.settings = settings;
    }

    // Static factory - readable way to create new aggregates
    static DeviceConfigurationEditor createNewDevice(String deviceId) {
        return new DeviceConfigurationEditor(
                deviceId,
                new ArrayList<>(),
                Ownership.unowned(),
                null,
                OpeningHours.alwaysOpened(),
                Settings.defaultSettings()
        );
    }

    // Public methods modifying internal state and emitting events
    void assignTo(Ownership ownership) {
        Objects.requireNonNull(ownership);

        // Ensuring idempotency of processed commands
        if (!Objects.equals(this.ownership, ownership)) {
            // Changing internal state
            this.ownership = ownership;
            // Formulating event and storing event for emission during persistence
            events.add(new DomainEvent.OwnershipUpdated(deviceId, ownership));

            // Ensuring additional business rules
            if (ownership.isUnowned()) {
                resetToDefaults();
            }
        }
    }

    void updateLocation(Location location) {
        if (!Objects.equals(this.location, location)) {
            this.location = location;
            events.add(new DomainEvent.LocationUpdated(deviceId, location));
        }
    }

    void updateOpeningHours(OpeningHours openingHours) {
        Objects.requireNonNull(openingHours);

        if (!Objects.equals(this.openingHours, openingHours)) {
            this.openingHours = openingHours;
            events.add(new DomainEvent.OpeningHoursUpdated(deviceId, openingHours));
        }
    }

    void updateSettings(Settings settings) {
        Objects.requireNonNull(settings);

        // Business rule: if showOnMap is true, must have location and publicAccess
        if (settings.showOnMap() && (location == null || !settings.publicAccess())) {
            throw new IllegalArgumentException("Cannot show on map without location and public access");
        }

        if (!Objects.equals(this.settings, settings)) {
            this.settings = settings;
            events.add(new DomainEvent.SettingsUpdated(deviceId, settings));
        }
    }

    // Private method implementing business rule
    private void resetToDefaults() {
        updateLocation(null);
        updateOpeningHours(OpeningHours.alwaysOpened());
        updateSettings(Settings.defaultSettings());
    }

    // Method creating state snapshot
    DeviceConfiguration toDeviceConfiguration() {
        Violations violations = checkViolations();
        Visibility visibility = Visibility.basedOn(
                violations.isValid() && settings.publicAccess(),
                settings.showOnMap()
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

    private Violations checkViolations() {
        return Violations.builder()
                .operatorNotAssigned(ownership.operator() == null)
                .providerNotAssigned(ownership.provider() == null)
                .locationMissing(location == null)
                .showOnMapButMissingLocation(settings.showOnMap() && location == null)
                .showOnMapButNoPublicAccess(settings.showOnMap() && !settings.publicAccess())
                .build();
    }
}
