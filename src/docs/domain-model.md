# Domain Model

The domain model follows Domain-Driven Design tactical patterns: **Aggregate**, **Value Object**, **Domain Event**, and **Service**. The aggregate is the core building block that enforces business invariants and emits events for state changes.

---

## Aggregate (`Device`)

The `Device` aggregate encapsulates all device configuration business rules. It is package-private — never exposed outside its bounded context.

**How to implement:**

- Name the aggregate with a business term, no suffix (e.g. `Device`, not `DeviceAggregate`)
- Keep the class package-private (`class`, not `public class`)
- Use immutable value objects for all fields except `events`
- The aggregate does not expose field getters; use snapshot methods instead
- Business rule methods are package-private and named after the business operation (e.g. `assignTo`, `updateLocation`)
- Collect emitted `DomainEvent` records in a mutable `List<DomainEvent> events` field
- Provide a `static` factory method (e.g. `newDevice`) for creation
- Provide a `toDeviceConfiguration()` method returning a read-only snapshot
- Enforce invariants as private methods (e.g. `checkViolations`)

```java
@AllArgsConstructor
class Device {
    final String deviceId;
    final List<DomainEvent> events;
    private Ownership ownership;
    private Location location;
    private OpeningHours openingHours;
    private Settings settings;

    static Device newDevice(String deviceId) {
        return new Device(
                deviceId,
                new ArrayList<>(),
                Ownership.unowned(),
                null,
                OpeningHours.alwaysOpened(),
                Settings.defaultSettings()
        );
    }

    void assignTo(Ownership ownership) {
        Objects.requireNonNull(ownership);
        if (!Objects.equals(this.ownership, ownership)) {
            this.ownership = ownership;
            events.add(new OwnershipUpdated(deviceId, ownership));
            if (ownership.isUnowned()) {
                resetToDefaults();
            }
        }
    }

    void updateLocation(Location location) {
        if (!Objects.equals(this.location, location)) {
            this.location = location;
            events.add(new LocationUpdated(deviceId, location));
        }
    }

    void updateOpeningHours(OpeningHours openingHours) {
        Objects.requireNonNull(openingHours);
        if (!Objects.equals(this.openingHours, openingHours)) {
            this.openingHours = openingHours;
            events.add(new OpeningHoursUpdated(deviceId, openingHours));
        }
    }

    void updateSettings(Settings settings) {
        Objects.requireNonNull(settings);
        Settings merged = this.settings.merge(settings);
        if (!Objects.equals(this.settings, merged)) {
            this.settings = merged;
            events.add(new SettingsUpdated(deviceId, this.settings));
        }
    }

    void resetToDefaults() {
        updateLocation(null);
        updateOpeningHours(OpeningHours.alwaysOpened());
        updateSettings(Settings.defaultSettings());
    }

    private Violations checkViolations() {
        return Violations.builder()
                .operatorNotAssigned(ownership.operator() == null)
                .providerNotAssigned(ownership.provider() == null)
                .locationMissing(location == null)
                .showOnMapButMissingLocation(settings.isShowOnMap() && location == null)
                .showOnMapButNoPublicAccess(settings.isShowOnMap() && !settings.isPublicAccess())
                .build();
    }

    DeviceConfiguration toDeviceConfiguration() {
        Violations violations = checkViolations();
        Visibility visibility = Visibility.basedOn(
                violations.isValid() && settings.isPublicAccess(),
                settings.isShowOnMap()
        );
        return new DeviceConfiguration(
                deviceId, ownership, location, openingHours, settings, violations, visibility
        );
    }
}
```

**Best practices:**
- Aggregate controls access to its internal state — all mutations go through named methods
- Every meaningful state change emits a `DomainEvent` — enables event sourcing, projections, audit
- Business invariants are enforced inside the aggregate, not in services or controllers
- The aggregate constructor accepts all fields; factory methods encapsulate creation logic
- Use `Objects.equals` for idempotent updates — no event emitted if value unchanged

**NOGO:**
- Do not make aggregate fields public — expose through snapshot or specific query methods only
- Do not place domain events as aggregate fields — they are collected and cleared on save
- Do not add getters like `getOwnership()` — use `toDeviceConfiguration()` for external reads
- Do not add business logic in services or controllers
- Do not expose the aggregate class as `public` — keep it package-private within its context

---

## Value Object

Value objects are immutable records that represent descriptive aspects of the domain. They have no identity and are compared by value.

**How to implement:**

- Use `public record` for value objects
- Make them `public` when they appear in public port signatures or read models
- Keep them package-private (`record` without `public`) when used only internally
- Provide static factory methods for named construction (e.g. `Ownership.unowned()`)
- Keep fields simple — prefer flattening over deep nesting
- Implement behavior related to the value directly on the record (e.g. `Settings.merge`, `Visibility.basedOn`)

```java
public record Ownership(String operator, String provider) {

    public static Ownership unowned() {
        return new Ownership(null, null);
    }

    public static Ownership of(String operator, String provider) {
        return new Ownership(operator, provider);
    }

    @JsonIgnore
    public boolean isUnowned() {
        return operator == null && provider == null;
    }

    @JsonIgnore
    public boolean isOwned() {
        return operator != null && provider != null;
    }
}
```

```java
@Builder(toBuilder = true)
record Settings(
        Boolean autoStart,
        Boolean remoteControl,
        Boolean billing,
        Boolean reimbursement,
        Boolean showOnMap,
        Boolean publicAccess) {

    static Settings defaultSettings() { ... }

    public Settings merge(Settings other) {
        SettingsBuilder merged = this.toBuilder();
        ofNullable(other.autoStart).ifPresent(merged::autoStart);
        // ... other fields
        return merged.build();
    }
}
```

```java
record Visibility(boolean roamingEnabled, ForCustomer forCustomer) {

    static Visibility basedOn(boolean usable, boolean showOnMap) {
        return new Visibility(usable, ForCustomer.calculateForCustomer(usable, showOnMap));
    }

    enum ForCustomer {
        USABLE_AND_VISIBLE_ON_MAP,
        USABLE_BUT_HIDDEN_ON_MAP,
        INACCESSIBLE_AND_HIDDEN_ON_MAP;

        private static ForCustomer calculateForCustomer(boolean usable, boolean showOnMap) { ... }
    }
}
```

**Best practices:**
- Records give `equals`, `hashCode`, `toString` for free — ideal for value objects
- Use `@Builder(toBuilder = true)` for objects with many optional fields
- Keep value objects hierarchy shallow: prefer flat records over nested
- Use `@JsonIgnore` for derived boolean methods that should not be serialized

**NOGO:**
- Do not add mutable state (`@Setter`, plain fields) to value objects
- Do not put JPA annotations on value objects — map them in the repository adapter
- Do not extend value objects or make them implement complex interfaces
- Do not add business logic that requires external dependencies

---

## Domain Event

Domain events capture what happened in the aggregate. They are immutable records stored in JSONB for event sourcing and published via `ApplicationEventPublisher` for projections.

**How to implement:**

- Define a `public interface` as the event marker type with `@JsonTypeInfo` + `@JsonSubTypes`
- Define each event as a `record` nested inside the interface implementing it
- Each event record must include the aggregate ID and the relevant value object
- Use `EventTypes` utility to map event classes to versioned type names (e.g. `OwnershipUpdated_v2`)
- For backward compatibility, implement `LegacyDomainEvent` interface with a `normalise()` method
- Events are published in the repository `save()` method via `ApplicationEventPublisher`

```java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DomainEvent.OwnershipUpdated.class, name = "OwnershipUpdated_v2"),
        @JsonSubTypes.Type(value = LegacyEvents.OwnershipUpdatedV1.class, name = "OwnershipUpdated_v1"),
        @JsonSubTypes.Type(value = DomainEvent.OpeningHoursUpdated.class, name = "OpeningHoursUpdated_v1"),
        @JsonSubTypes.Type(value = DomainEvent.LocationUpdated.class, name = "LocationUpdated_v1"),
        @JsonSubTypes.Type(value = DomainEvent.SettingsUpdated.class, name = "SettingsUpdated_v1")
})
public interface DomainEvent {
    record LocationUpdated(String deviceId, Location location) implements DomainEvent {}
    record OpeningHoursUpdated(String deviceId, OpeningHours openingHours) implements DomainEvent {}
    record OwnershipUpdated(String deviceId, Ownership ownership) implements DomainEvent {}
    record SettingsUpdated(String deviceId, Settings settings) implements DomainEvent {}
}
```

**Best practices:**
- Events are named in past tense (e.g. `OwnershipUpdated`, not `UpdateOwnership`)
- Events are immutable — use records
- Each event carries the aggregate ID and the new value (full replacement, not diff)
- Use `@JsonSubTypes` for polymorphic JSON serialization into JSONB columns
- Version event types with `_v1`, `_v2` suffixes for schema evolution

**NOGO:**
- Do not put business logic in event records
- Do not skip events for "trivial" state changes — every mutation must emit an event
- Do not reference JPA entities or persistence concerns in event definitions

---

## Read Model Snapshot (`DeviceConfiguration`)

The aggregate produces a read-only snapshot via `toDeviceConfiguration()`. This is the output DTO returned to controllers and used in projections.

**How to implement:**

- Define as a `public record` containing all value objects that comprise the full state
- Computed fields (violations, visibility) are populated by the aggregate, not re-computed outside
- The snapshot is created inside the aggregate, not in the service layer

```java
public record DeviceConfiguration(
        String deviceId,
        Ownership ownership,
        Location location,
        OpeningHours openingHours,
        Settings settings,
        Violations violations,
        Visibility visibility) {
}
```

**NOGO:**
- Do not place the snapshot as a field inside the aggregate — aggregate holds raw value objects
- Do not compute violations or visibility in the controller or service

---

## Command DTO (`UpdateDevice`)

Commands carry input data from HTTP controllers to the service layer. They know how to apply themselves to the aggregate.

**How to implement:**

- Define as a `public record` with `@Builder`
- Only include fields that can be updated (all optional — null means "don't update")
- Provide an `apply(Device)` method that delegates to the aggregate's business methods
- Use `@Valid` on nested value objects for Jakarta Validation

```java
@Builder
public record UpdateDevice(
        @Valid Location location,
        @Valid OpeningHours openingHours,
        @Valid Settings settings,
        @Valid Ownership ownership) {

    public static UpdateDevice use(Ownership ownership, Location location) {
        return builder().location(location).ownership(ownership).build();
    }

    public void apply(Device device) {
        if (location != null) device.updateLocation(location);
        if (openingHours != null) device.updateOpeningHours(openingHours);
        if (settings != null) device.updateSettings(settings);
        if (ownership != null) device.assignTo(ownership);
    }
}
```

**Best practices:**
- Command DTO is a record with `@Builder` — clear construction with partial updates
- The `apply(Device)` method keeps update logic in one place, not scattered across the service
- Null fields mean "do not update" — enables PATCH semantics naturally

---

## Domain Model Unit Testing

Domain model tests operate directly on the aggregate and value objects — no Spring context, no database.

**How to implement:**

- Test class is package-private in the same package as the aggregate
- Use `DeviceFixture` for test data builders (static factory methods)
- Use `DeviceConfigurationAssert` for fluent custom AssertJ assertions
- Test each business method independently — given/when/then structure
- Verify both state changes AND emitted events

```java
class DeviceTest {

    @Test
    void assignStationToOwner() {
        Device device = givenDevice();
        device.assignTo(someOtherOwnership());

        assertThat(device)
                .hasOwnership(someOtherOwnership())
                .hasNoViolations();
    }

    @Test
    void resetOwnership() {
        Device device = givenDevice();
        device.assignTo(Ownership.unowned());

        assertThat(device)
                .hasOwnership(Ownership.unowned())
                .hasSettings(Settings.defaultSettings())
                .hasLocation(null)
                .hasOpeningHours(OpeningHours.alwaysOpened())
                .hasViolationsLikeNotConfiguredDevice();
    }

    @Test
    void mergeSettings() {
        Device device = givenDevice();
        device.updateSettings(settingsForPublicDevice());
        device.updateSettings(settingsWithAutoStartOnly());

        assertThat(device)
                .hasSettings(Settings.builder()
                        .publicAccess(true).showOnMap(true).autoStart(true)
                        .remoteControl(false).billing(false).reimbursement(false))
                .hasNoViolations();
    }
}
```

**Best practices:**
- Unit tests for the aggregate are the fastest and most valuable tests — run in milliseconds
- Use descriptive test names in business language, not implementation language
- Given/when/then structure is visible through fixture call → method call → assertion chain

**NOGO:**
- Do not test domain logic only through integration tests
- Do not use mutable shared fixture instances — call factory methods for each test
- Do not test implementation details (private methods, internal state) — test business behavior

---

## Test Fixture (`DeviceFixture`)

Static factory methods that produce pre-configured aggregate instances and value objects for tests.

**How to implement:**

- `public class` with static methods returning domain objects
- Methods are named descriptively: givenDevice, location, ownership, settingsForPublicDevice
- Use `randomId()` to generate unique IDs with `UUID.randomUUID()`

```java
public class DeviceFixture {
    public static String randomId() { return UUID.randomUUID().toString(); }

    public static Device givenDevice() {
        return new Device(randomId(), new ArrayList<>(),
                ownership(), location(), OpeningHours.alwaysOpened(), Settings.defaultSettings());
    }

    public static Device givenStepByStepConfiguredDevice() {
        Device device = Device.newDevice(randomId());
        device.assignTo(ownership());
        device.updateLocation(location());
        device.updateOpeningHours(OpeningHours.alwaysOpened());
        device.updateSettings(Settings.defaultSettings());
        return device;
    }

    public static Ownership ownership() {
        return new Ownership("Devicex.nl", "public-devices");
    }

    public static Location location() { ... }

    public static Settings settingsForPublicDevice() {
        return Settings.defaultSettings().toBuilder().showOnMap(true).publicAccess(true).build();
    }
}
```

**NOGO:**
- Do not use mutable shared fixture instances across tests — always create fresh via factory methods
- Do not put test fixtures inside the production source tree

---

## Custom Assertion (`DeviceConfigurationAssert`)

Fluent custom assertion wrapping AssertJ for readable domain-specific test assertions.

**How to implement:**

- Extends no class — wraps AssertJ `Assertions.assertThat` internally
- Provides overloaded static `assertThat` methods for `Device`, `DeviceConfiguration`, and `Optional<DeviceConfiguration>`
- Each assertion method returns `this` for fluent chaining
- Name methods in business language: `hasNoViolations`, `hasViolationsLikeNotConfiguredDevice`, `isNotVisible`

```java
public class DeviceConfigurationAssert {
    private final DeviceConfiguration device;

    private DeviceConfigurationAssert(DeviceConfiguration device) { this.device = device; }

    public static DeviceConfigurationAssert assertThat(Device actual) {
        return new DeviceConfigurationAssert(actual.toDeviceConfiguration());
    }

    public static DeviceConfigurationAssert assertThat(DeviceConfiguration actual) {
        return new DeviceConfigurationAssert(actual);
    }

    public static DeviceConfigurationAssert assertThat(Optional<DeviceConfiguration> actual) {
        Assertions.assertThat(actual).isNotEmpty();
        return new DeviceConfigurationAssert(actual.get());
    }

    public DeviceConfigurationAssert hasSettings(Settings expected) { ... return this; }
    public DeviceConfigurationAssert hasOwnership(Ownership expected) { ... return this; }
    public DeviceConfigurationAssert hasLocation(Location expected) { ... return this; }
    public DeviceConfigurationAssert hasNoViolations() { ... return this; }
    public DeviceConfigurationAssert hasViolationsLikeNotConfiguredDevice() { ... return this; }
    public DeviceConfigurationAssert isNotVisible() { ... return this; }
}
```

**Best practices:**
- Custom assertions produce readable test failures: `assertThat(device).hasNoViolations()` vs generic `assertThat(device.violations().isValid()).isTrue()`
- The overloaded `assertThat` removes boilerplate in tests — pass Device directly

**NOGO:**
- Do not add generic `getField()` style assertions — keep them business-meaningful
- Do not extend AssertJ classes — simple delegation is more maintainable
