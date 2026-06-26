# Ports (Primary and Secondary)

The project uses hexagonal architecture with ports for dependency inversion. Ports are interfaces that define contracts between the domain and the outside world.

---

## Repository Port — Secondary Port

Repository ports are package-private interfaces in the context package. They abstract persistence — adapters implement them.

**How to implement:**

- Define as `interface` in the context package, package-private (no `public`)
- Methods named domain-operationally: `get(id)`, `save(aggregate)` — not generic CRUD
- Return domain objects (`Device`), never JPA entities
- Do not extend Spring Data interfaces — those belong in the adapter

```java
interface DeviceRepository {
    Optional<Device> get(String deviceId);
    void save(Device device);
}
```

**Best practices:**
- Port is owned by the domain, implemented by adapters
- Method signatures use domain types only
- Repository is a secondary port — the domain calls it, adapters implement it

**NOGO:**
- Do not expose JPA entities or `Pageable` in port signatures
- Do not make repository ports `public` — keep them package-private
- Do not add `@Repository` on the port interface — annotate the adapter implementation

---

## Service — Primary Port

Service classes are the primary port. They are `@Service @Transactional` public classes that orchestrate aggregate lifecycle. Controllers depend on services, never on repositories directly.

**How to implement:**

- `public class` annotated with `@Service @Transactional @RequiredArgsConstructor`
- Inject the repository port — the service never references adapter classes
- Keep thin: delegate to aggregate methods, then save

```java
@Service
@Transactional
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository repository;

    public Optional<DeviceConfiguration> getDevice(String deviceId) {
        return repository.get(deviceId)
                .map(Device::toDeviceConfiguration);
    }

    public DeviceConfiguration createNewDevice(String deviceId, UpdateDevice update) {
        Device device = Device.newDevice(deviceId);
        update.apply(device);
        repository.save(device);
        return device.toDeviceConfiguration();
    }

    public Optional<DeviceConfiguration> updateDevice(String deviceId, UpdateDevice update) {
        return repository.get(deviceId)
                .map(device -> {
                    update.apply(device);
                    repository.save(device);
                    return device.toDeviceConfiguration();
                });
    }
}
```

**Best practices:**
- Service manages aggregate lifecycle: load → mutate → save
- Service is `@Transactional` — the save and event publishing happen in one transaction
- Controllers never access the repository port — only through the service

**NOGO:**
- Do not put business logic in services — keep invariants in the aggregate
- Do not inject persistence adapters directly — inject the port
- Do not make services package-private — they are the public API of the context

---

## Secondary Port for External System Access

For accessing other contexts or external systems, define interfaces in the consuming context.

```java
public interface KnownDevices {
    enum State { UNKNOWN, EXISTING }
    State get(String deviceId);
}
```

**How to implement:**

- `public interface` if consumed across contexts
- The interface stays in the consuming context (`communication`)
- Implementation is an event-driven projection (`KnownDevicesProjection`) or a mediator

**NOGO:**
- Do not reference sibling context services directly — use interfaces + mediators

---

## Cross-Context Contracts

When one context needs data from another, it defines a public interface (port). The providing context implements it via a projection or mediator adapter. No direct service-to-service calls across contexts.

| Contract | Consumer | Provider | Implementation |
|----------|----------|----------|---------------|
| `KnownDevices` | `communication` | `communication` (self) | `KnownDevicesProjection` listens to device events |
| `BootNotification` (event) | `search` | `communication` | Projection listens to event |
| `DeviceConfiguration` (event) | `search`, `communication` | `device` | Spring `ApplicationEventPublisher` |

**NOGO:**
- Do not call `DeviceService` from `CommunicationService` — use mediator + events
