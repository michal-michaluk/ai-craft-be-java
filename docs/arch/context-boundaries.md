# Context Boundaries

The project is a modular monolith organized into bounded contexts. Each context owns its domain logic, persists its own data, and communicates with other contexts through explicit contracts.

---

## Bounded Contexts

| Context | Package | Owns | Dependencies |
|---------|---------|------|-------------|
| **Device** | `device` | `Device` aggregate, value objects, domain events, 3 persistence strategies | `tools` |
| **Search** | `search` | Read models, query endpoints | `device` (events/value objects), `communication` (events/value objects) |
| **Communication** | `communication` | Boot notification handling, protocol adapters | `device` (events/value objects), `intervals` (via service/port) |
| **Intervals** | `intervals` | Interval policy rules | `communication` (`BootNotification` value object) |
| **Tools** | `tools` | JSON config, event types, Jackson mapper | None (cross-cutting) |

---

## Dependency Rules

Enforced by ArchUnit in each context's `ArchitectureOf{Context}Test`:

1. **No cross-context imports of internal model classes** — only import public value objects and events
2. **No direct repository/service calls across contexts** — use events or mediator
3. **Tools is the only shared kernel** — all contexts may depend on `devices.configuration.tools`
4. **Dependencies point inward** — outer contexts depend on inner contexts, never the reverse

Violations cause ArchUnit test failures.

---

## Shared Kernel

The `tools` package is the shared kernel — classes that any context may use:

- `JsonConfiguration` — Jackson 3 ObjectMapper
- `EventTypes` — Event type mapping
- `LegacyDomainEvent` — Event normalization interface
- `FeatureConfiguration` — Generic JSONB configuration service

**Adding to shared kernel:**
- New types in `tools` must be truly cross-cutting — utility, JSON, or generic infrastructure
- When adding new shared kernel types, update `ArchitectureDescription` predicates

---

## Public Contracts (Shared Kernel Exposed)

Each context explicitly declares which types it exposes to other contexts. This is configured in the `ArchitectureOf{Context}Test` as `sharedKernelExposed`.

| Context | Exposed Types |
|---------|--------------|
| **Device** | `UpdateDevice`, `DeviceConfiguration`, `Ownership`, `Location` |
| **Search** | (none) |
| **Communication** | `BootNotification`, `DeviceStatuses`, `KnownDevices` |
| **Intervals** | (none) |

**How to implement:**

```java
// In ArchitectureOfDeviceContextTest
public static final DescribedPredicate<JavaClass> sharedKernelExposed = belongToAnyOf(
        UpdateDevice.class, DeviceConfiguration.class,
        Ownership.class, Location.class
);
```

Types NOT in this list are considered internal — ArchUnit prevents access from other contexts.

**NOGO:**
- Do not expose internal value objects as public — make them package-private if not needed by other contexts
- Do not import classes from other contexts that are not in their `sharedKernelExposed`
- Do not reference sibling context services or repositories directly — use events or mediator
- Do not add new shared kernel types without updating architecture tests
