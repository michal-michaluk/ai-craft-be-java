# Adapter: Mediator (Cross-Context Orchestration)

The mediator pattern prevents direct dependencies between bounded contexts. Instead of calling sibling context services or repositories directly, contexts communicate through events or mediator adapters.

---

## Communication Mechanisms

| Mechanism | How | When |
|-----------|-----|------|
| Domain Events via `ApplicationEventPublisher` | Write-side publishes events; projections in other contexts listen | Projection updates, cross-context state synchronization |
| Shared Port Interface | One context defines an interface; another implements it | Querying state from another context |
| Mediator Adapter (mediators package) | Dedicated adapter class that orchestrates across contexts | Complex multi-step flows |

---

## Event-Based Cross-Context Flow

The most common cross-context communication is event-driven:

1. `DeviceController` calls `DeviceService.updateDevice()`
2. `DeviceDocumentWithHistoryRepository.save()` calls `publisher.publishEvent(device.toDeviceConfiguration())`
3. `ReadModelsProjection.handle(DeviceConfiguration)` in the `search` context receives the event
4. `KnownDevicesProjection.handleDeInstallation(DeviceConfiguration)` in the `communication` context receives the event

```
device context                              search context
┌──────────────────────┐                  ┌──────────────────────┐
│ DeviceController     │                  │ DeviceReadsController│
│   → DeviceService    │                  │   → ReadModels       │
│     → DeviceRepo     │                  │     Projection       │
│       → publishEvent │  ──────────────► │       → @EventListener│
│         (DeviceConfig│  via Spring      │         → update      │
│          uration)    │  EventBus        │           device_reads│
└──────────────────────┘                  └──────────────────────┘
                              │
                              │
                    communication context
                    ┌──────────────────────┐
                    │ KnownDevicesProjection│
                    │   → @EventListener   │
                    │     → update         │
                    │       known_device   │
                    └──────────────────────┘
```

**How to implement cross-context event flow:**

- The publishing context emits domain events from the repository `save()` method
- Receiving contexts use `@Component @Transactional @AllArgsConstructor` classes with `@EventListener`
- Receiving contexts only import event/value objects from the publishing context — never internal classes or services

```java
// In communication context — listens to device events
@Component
@Transactional
@AllArgsConstructor
class KnownDevicesProjection implements KnownDevices {
    // ...
    @EventListener
    public void handleDeInstallation(DeviceConfiguration event) {
        if (event.ownership().isUnowned()) {
            put(event.deviceId(), State.UNKNOWN);
        }
    }
}
```

---

## Shared Port Cross-Context Flow

When one context needs to synchronously query state from another context:

1. Consuming context defines a `public interface` port
2. Providing context implements the port via a projection or dedicated adapter
3. The implementation accesses the read model, never the write model

```java
// In communication context — defines the port
public interface KnownDevices {
    enum State { UNKNOWN, EXISTING }
    State get(String deviceId);
}

// In communication context — self-implementation via projection
@Primary
@Component
@Transactional
class KnownDevicesProjection implements KnownDevices {
    public State get(String deviceId) {
        return repository.findById(deviceId)
                .map(KnownDeviceEntity::state)
                .orElse(State.UNKNOWN);
    }
}
```

**Best practices:**
- Keep the port interface in the consuming context — the dependency points toward the consumer
- Implement the port with a projection or mediator (not a direct service call)
- `@Primary` ensures the correct implementation is injected

---

## Explicit Mediator Adapter

For complex multi-step cross-context flows that cannot be expressed as simple event reactions, create a dedicated mediator class in a `mediators` package.

```java
// Example pattern (not yet in codebase — for new flows):
package devices.configuration.mediators;

@Component
@RequiredArgsConstructor
class DeviceRegistrationMediator {
    private final DeviceService deviceService;
    private final CommunicationService communicationService;

    public void registerDevice(String deviceId, UpdateDevice update) {
        DeviceConfiguration config = deviceService.createNewDevice(deviceId, update);
        BootNotification boot = new BootNotification(deviceId, IoT20, ...);
        communicationService.handleBoot(boot);
    }
}
```

**When to use mediators:**
- A single API call needs to orchestrate across 2+ contexts
- Compensating actions are needed on failure
- The flow is a business transaction spanning multiple aggregates

**NOGO:**
- Do not call repositories or services of other contexts directly from controllers
- Do not import internal classes from other contexts — only use public ports and value objects
- Do not use mediators when a simple event listener suffices
- Do not put business logic in mediators — they orchestrate, not decide
