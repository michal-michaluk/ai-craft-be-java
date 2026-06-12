package devices.configuration.device;

import devices.configuration.tools.EventTypes;
import devices.configuration.tools.LastEvents;
import devices.configuration.tools.LegacyDomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static devices.configuration.device.DomainEvent.*;

@Repository
@AllArgsConstructor
class DeviceEventSourcingRepository implements DeviceRepository {

    private final EventRepository repository;
    private final ApplicationEventPublisher publisher;

    @Override
    public Optional<Device> get(String deviceId) {
        List<DomainEvent> history = repository.findByDeviceId(deviceId).stream()
                .map(DeviceEventEntity::getEvent)
                .map(LegacyDomainEvent::normalise)
                .collect(Collectors.toList());
        if (history.isEmpty()) {
            return Optional.empty();
        }
        Collections.reverse(history);
        LastEvents events = LastEvents.fromHistoryOf(history);
        Device device = new Device(deviceId, new ArrayList<>(),
                events.getOrNull(OwnershipUpdated.class, OwnershipUpdated::ownership),
                events.getOrNull(LocationUpdated.class, LocationUpdated::location),
                events.getOrDefault(OpeningHoursUpdated.class, OpeningHoursUpdated::openingHours, OpeningHours.alwaysOpened()),
                events.getOrDefault(SettingsUpdated.class, SettingsUpdated::settings, Settings.defaultSettings())
        );
        return Optional.of(device);
    }

    @Override
    public void save(Device device) {
        List<DomainEvent> events = emittedFrom(device);
        events.forEach(event -> {
            repository.save(new DeviceEventEntity(
                    device.deviceId,
                    EventTypes.of(event),
                    event));
            publisher.publishEvent(event);
        });
        if (!events.isEmpty()) {
            publisher.publishEvent(device.toDeviceConfiguration());
        }
    }

    private static List<DomainEvent> emittedFrom(Device device) {
        List<DomainEvent> emitted = List.copyOf(device.events);
        device.events.clear();
        return emitted;
    }

    @Repository
    interface EventRepository extends CrudRepository<DeviceEventEntity, UUID> {
        @Query(value = """
                select distinct on (type) *
                from device_events
                where device_id = :deviceId
                order by type, time desc""", nativeQuery = true)
        List<DeviceEventEntity> findByDeviceId(String deviceId);
    }

    @Entity
    @Table(name = "device_events")
    @NoArgsConstructor
    static class DeviceEventEntity {
        @Id
        private UUID id;
        private String deviceId;
        private String type;
        private Instant time;
        @Getter
        @JdbcTypeCode(SqlTypes.JSON)
        private DomainEvent event;

        DeviceEventEntity(String deviceId, EventTypes.Type type, DomainEvent event) {
            this.id = UUID.randomUUID();
            this.deviceId = deviceId;
            this.type = type.type();
            this.time = Instant.now();
            this.event = event;
        }
    }
}
