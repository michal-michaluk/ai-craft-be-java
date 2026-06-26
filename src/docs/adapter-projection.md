# Adapter: Projection (Event-Driven Read Models)

Projections are event listeners that update read models when domain events are published. They implement the CQRS read side — the write model publishes events, projections consume them to keep query-optimized tables current.

---

## How Event Publishing Works

Domain events are published in the repository `save()` method via Spring's `ApplicationEventPublisher`:

1. Aggregate `Device` emits events into `device.events` list
2. Repository collects emitted events, persists them, then calls `publisher.publishEvent(event)`
3. Repository also publishes a combined `device.toDeviceConfiguration()` snapshot event
4. Projections listen with `@EventListener` and update read model tables

---

## Read Models Projection

`ReadModelsProjection` maintains the `device_reads` table — a JSONB-based read model combining data from multiple event types.

**How to implement:**

- `@Component @Transactional @AllArgsConstructor` class
- Method-level `@EventListener` for each event type handled
- Each handler loads the existing entity (or creates new), updates relevant fields, saves
- Combined read methods (`findById`, `findAllPins`, `findAllSummary`) are `@Transactional(readOnly = true)`

```java
@Component
@Transactional
@AllArgsConstructor
class ReadModelsProjection {

    private final DeviceReadsRepository repository;

    @EventListener
    public void handle(DeviceConfiguration details) {
        DeviceReadsEntity entity = repository.findById(details.deviceId())
                .orElseGet(() -> new DeviceReadsEntity(details.deviceId()));
        entity.setOwnership(details.ownership())
                .setDetails(details)
                .setPin(DevicePin.ofNullable(details, entity.getStatuses()))
                .setSummary(DeviceSummary.ofNullable(details, entity.getStatuses()));
        repository.save(entity);
    }

    @EventListener
    public void handle(BootNotification boot) {
        DeviceReadsEntity entity = repository.findById(boot.deviceId())
                .orElseGet(() -> new DeviceReadsEntity(boot.deviceId()));
        entity.setBoot(boot);
        repository.save(entity);
    }

    @EventListener
    public void handle(DeviceStatuses statuses) {
        DeviceReadsEntity entity = repository.findById(statuses.deviceId())
                .orElseGet(() -> new DeviceReadsEntity(statuses.deviceId()));
        entity.setStatuses(statuses)
                .setPin(DevicePin.ofNullable(entity.getDetails(), statuses))
                .setSummary(DeviceSummary.ofNullable(entity.getDetails(), statuses));
        repository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<DeviceDetails> findById(String deviceId) {
        return repository.findById(deviceId)
                .map(entity -> new DeviceDetails(entity.details, entity.boot));
    }

    @Transactional(readOnly = true)
    public Page<DeviceSummary> findAllSummary(String provider, Pageable pageable) {
        return repository.findAllByProvider(provider, pageable)
                .map(DeviceReadsEntity::getSummary);
    }
}
```

**Read model entity schema:**

```
device_reads
    device_id   varchar(255) PK
    version     bigint
    operator    varchar(255)       (indexed for queries)
    provider    varchar(255)       (indexed for queries)
    pin         jsonb               (DevicePin)
    summary     jsonb               (DeviceSummary)
    details     jsonb               (DeviceConfiguration)
    statuses    jsonb               (DeviceStatuses)
    boot        jsonb               (BootNotification)
```

**Best practices:**
- Each event handler is idempotent — loading, merging, saving
- Handlers use `findById` then `orElseGet` to create new entities — no pre-existing row needed
- `setPin` and `setSummary` are recomputed whenever either `details` or `statuses` changes
- JSONB columns allow flexible schema — add new columns without migrations

---

## Known Devices Projection

`KnownDevicesProjection` maintains which devices are known. It both implements the `KnownDevices` port AND listens to events.

```java
@Primary
@Component
@Transactional
@AllArgsConstructor
class KnownDevicesProjection implements KnownDevices {
    private final JpaRepository repository;

    @Override
    public State get(String deviceId) {
        return repository.findById(deviceId)
                .map(KnownDeviceEntity::state)
                .orElse(State.UNKNOWN);
    }

    @EventListener
    public void handleDeInstallation(DeviceConfiguration event) {
        if (event.ownership().isUnowned()) {
            put(event.deviceId(), State.UNKNOWN);
        }
    }
}
```

**Best practices:**
- A projection can implement a port (`KnownDevices`) — it is both a listener AND a query provider
- `@Primary` ensures this implementation is used when the port is injected
- The projection combines data from a single event source — `DeviceConfiguration` with unowned ownership

**NOGO:**
- Do not put business logic in projections — they are pure data transformers
- Do not call repositories or services from projections — only update the read model table
- Do not skip event handling because "the data might already be correct" — always merge
- Do not let projections fail silently — they run in the same transaction as the event publisher
