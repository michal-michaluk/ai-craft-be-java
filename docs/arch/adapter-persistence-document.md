# Adapter: Persistence — Document (JSONB)

Document persistence stores the entire aggregate state as a single JSONB blob. The project has two document-based implementations: a simple one (no event history) and a hybrid one (document snapshot + event history).

---

## Device Document Schema

The `device_document` table stores the aggregate as a JSONB column with optimistic locking.

```
device_document
    device_id   varchar(255) PRIMARY KEY
    version     bigint NOT NULL DEFAULT 1   (optimistic lock)
    device      jsonb  NOT NULL
```

Created via Liquibase changeset `0002-device-document`.

---

## Simple Document Repository

No event history — just persist/load the aggregate as JSONB.

**How to implement:**

- Inline `DocumentRepository` extending `CrudRepository` + `PagingAndSortingRepository`
- `get()` reads by ID and maps the JSONB entity back to the aggregate
- `save()` uses a read-then-write pattern: find existing or create new, set device, save

```java
@Repository
@AllArgsConstructor
class DeviceDocumentSimpleRepository implements DeviceRepository {

    private final DocumentRepository documents;

    @Override
    public Optional<Device> get(String deviceId) {
        return documents.findById(deviceId)
                .map(DeviceDocumentEntity::getDevice);
    }

    @Override
    public void save(Device device) {
        documents.save(documents.findById(device.deviceId)
                .orElseGet(() -> new DeviceDocumentEntity(device.deviceId))
                .setDevice(device)
        );
    }

    @Repository
    interface DocumentRepository
            extends CrudRepository<DeviceDocumentEntity, String>,
                    PagingAndSortingRepository<DeviceDocumentEntity, String> {}

    @Entity
    @Table(name = "device_document")
    @NoArgsConstructor
    static class DeviceDocumentEntity {
        @Id private String deviceId;
        @Version private long version;
        @Getter @JdbcTypeCode(SqlTypes.JSON)
        private Device device;

        public DeviceDocumentEntity setDevice(Device device) {
            this.device = device; return this;
        }
    }
}
```

**Best practices:**
- `@Version` enables optimistic locking — concurrent updates fail with `OptimisticLockException`
- The entity is a nested static class — no external visibility
- JSONB allows schema-less storage — the aggregate structure can evolve

---

## Document With History Repository (`@Primary`)

This is the default (`@Primary`) repository. It stores the full aggregate as a JSONB snapshot AND appends events to `device_events` for audit/history.

**How to implement:**

- Extends the document approach but additionally persists events
- Events are published individually AND as a combined `DeviceConfiguration` snapshot for projections
- The snapshot enables fast reads (single JSONB read) while events provide full audit trail

```java
@Primary
@Repository
@AllArgsConstructor
class DeviceDocumentWithHistoryRepository implements DeviceRepository {

    private final DocumentRepository documents;
    private final EventRepository events;
    private final ApplicationEventPublisher publisher;

    @Override
    public Optional<Device> get(String deviceId) {
        return documents.findById(deviceId)
                .map(DeviceDocumentEntity::getDevice);
    }

    @Override
    public void save(Device device) {
        List<DomainEvent> emitted = eventsEmittedFrom(device);
        documents.save(documents.findById(device.deviceId)
                .orElseGet(() -> new DeviceDocumentEntity(device.deviceId))
                .setDevice(device)
        );
        emitted.forEach(event -> events.save(
                new DeviceEventEntity(device.deviceId, event)));
        if (!emitted.isEmpty()) {
            publisher.publishEvent(device.toDeviceConfiguration());
        }
        emitted.forEach(publisher::publishEvent);
    }
}
```

**Best practices:**
- `@Primary` marks this as the default implementation — Spring injects this when multiple `DeviceRepository` beans exist
- The snapshot provides O(1) reads (single row fetch) while events provide O(n) audit
- Events and snapshot are persisted in the same transaction — consistency guaranteed

**NOGO:**
- Do not use the simple document repository as `@Primary` — it lacks event history
- Do not skip event publishing — projections depend on events to update read models
- Do not manually manage `@Version` — let Hibernate handle optimistic locking
- Do not expose JPA entity classes outside the repository file
