package devices.configuration.management;

// Marker interface or sealed interface
public sealed interface DomainEvent {
    String deviceId();

    // Concrete events as records
    record OwnershipUpdated(String deviceId, Ownership ownership) implements DomainEvent {}

    record LocationUpdated(String deviceId, Location location) implements DomainEvent {}

    record OpeningHoursUpdated(String deviceId, OpeningHours openingHours) implements DomainEvent {}

    record SettingsUpdated(String deviceId, Settings settings) implements DomainEvent {}
}
