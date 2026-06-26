# Adapter: Persistence — Event Sourcing

Event sourcing stores every state change as an append-only sequence of domain events. The current state is reconstructed by replaying events.

**When to use:** The `@Primary` repository is `DeviceDocumentWithHistoryRepository` (hybrid document + events). The event-sourcing-only repository is an alternative implementation used for specific testing or migration scenarios.

---

## Event Store Schema

The `device_events` table stores events as JSONB rows with an event type discriminator.

```
device_events
    id          uuid PRIMARY KEY
    device_id   varchar(255) NOT NULL
    type        varchar(255) NOT NULL
    time        timestamp    NOT NULL
    event       jsonb        NOT NULL
```

Created via Liquibase changeset `0003-device-events`.

---

## Event Sourcing Repository

**How to implement:**

- `@Repository` implementing the `DeviceRepository` port
- Use a nested `EventRepository` (Spring Data `CrudRepository`) for database access
- **Read:** Load all events for the device, sort newest-first, use `LastEvents` to get the latest of each type, then reconstruct the aggregate
- **Save:** Collect emitted events from the aggregate, persist each as a JSONB row, publish via `ApplicationEventPublisher`

```java
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
        if (history.isEmpty()) return Optional.empty();
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
                    device.deviceId, EventTypes.of(event), event));
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
        @Id private UUID id;
        private String deviceId;
        private String type;
        private Instant time;
        @Getter @JdbcTypeCode(SqlTypes.JSON)
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
```

**Best practices:**
- Use `SELECT DISTINCT ON (type)` to get the latest event per type — efficient replay
- `LegacyDomainEvent.normalise()` converts old event formats to current schema
- Events are collected via `List.copyOf(device.events)` + `clear()` — drain pattern
- Each event is published individually, plus a combined snapshot event for projections

---

## Event Types

The `EventTypes` utility maps Java classes to versioned type names (e.g. `OwnershipUpdated_v2`).

**How to implement:**

- Type names are derived from `@JsonSubTypes` annotations on the `DomainEvent` interface
- Initialized at startup in `JsonConfiguration.initEventTypes()` via `@PostConstruct`
- The type name format is `{EventName}_v{version}` — the `_v` segment is required for backward compatibility

```java
public class EventTypes {
    private static Map<Class<?>, Type> mapping;

    public static Type of(Object event) { return of(event.getClass()); }

    public record Type(String type, String version) {
        public static Type of(String typeName) {
            String[] parts = typeName.split("_v");
            // Requires _v separator
            return new Type(parts[0], parts[1]);
        }
    }
}
```

**NOGO:**
- Do not skip the `_v` in `@JsonSubTypes` name — the version suffix is required for schema evolution
- Do not store events without the type discriminator — it's required for deserialization
- Do not modify event records in ways that break deserialization of existing JSONB rows — add new event types instead

---

## Last Events Deduplication

`LastEvents` reconstructs the aggregate state by keeping only the latest event of each type.

```java
public static LastEvents fromHistoryOf(List<?> events) {
    return new LastEvents(events.stream()
            .collect(Collectors.toMap(
                    Object::getClass,
                    Function.identity(),
                    (last, previous) -> last   // keep latest
            )));
}
```

---

## Legacy Event Normalization

Old event formats implement `LegacyDomainEvent` and provide a `normalise()` method converting to the current record format.

```java
class LegacyEvents {
    public record OwnershipUpdatedV1(
            String deviceId, String operator, String provider
    ) implements LegacyDomainEvent {
        @Override
        public DomainEvent.OwnershipUpdated normalise() {
            return new DomainEvent.OwnershipUpdated(deviceId,
                    new Ownership(operator, provider));
        }
    }
}
```

**NOGO:**
- Do not delete old event record types if JSONB rows still exist — keep them with `LegacyDomainEvent`
- Do not rename existing event type names in `@JsonSubTypes` — add new version instead
