package devices.configuration.management;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEventTest {

    @Test
    void shouldCreateOwnershipUpdatedEvent() {
        Ownership ownership = Ownership.of("op", "prov");
        var event = new DomainEvent.OwnershipUpdated("device1", ownership);
        assertThat(event.deviceId()).isEqualTo("device1");
        assertThat(event.ownership()).isEqualTo(ownership);
        assertThat(event).isInstanceOf(DomainEvent.class);
    }

    @Test
    void shouldCreateLocationUpdatedEvent() {
        Location.Coordinates coords = new Location.Coordinates(1.0, 2.0);
        Location location = new Location("street", "1", "city", "123", "PL", coords);
        var event = new DomainEvent.LocationUpdated("device1", location);
        assertThat(event.deviceId()).isEqualTo("device1");
        assertThat(event.location()).isEqualTo(location);
        assertThat(event).isInstanceOf(DomainEvent.class);
    }

    @Test
    void shouldCreateOpeningHoursUpdatedEvent() {
        OpeningHours hours = OpeningHours.alwaysOpened();
        var event = new DomainEvent.OpeningHoursUpdated("device1", hours);
        assertThat(event.deviceId()).isEqualTo("device1");
        assertThat(event.openingHours()).isEqualTo(hours);
        assertThat(event).isInstanceOf(DomainEvent.class);
    }

    @Test
    void shouldCreateSettingsUpdatedEvent() {
        Settings settings = Settings.defaultSettings();
        var event = new DomainEvent.SettingsUpdated("device1", settings);
        assertThat(event.deviceId()).isEqualTo("device1");
        assertThat(event.settings()).isEqualTo(settings);
        assertThat(event).isInstanceOf(DomainEvent.class);
    }
}