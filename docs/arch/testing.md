# Testing

The project follows a test pyramid: fast unit tests at the base, service tests with fakes in the middle, integration tests with Testcontainers at the top.

---

## Test Pyramid

| Layer | Technology | Speed | What it tests |
|-------|-----------|-------|---------------|
| **Unit** | JUnit 5 + AssertJ | ms | Aggregate invariants, value objects, policy rules |
| **Service** | JUnit 5 + Fake Repo | ms | Service orchestration, command application |
| **Integration** | Testcontainers + Spring Boot | seconds | Repository persistence, projection updates |
| **Architecture** | ArchUnit | seconds | Package dependency rules, context isolation |
| **Contract** | Pact (consumer/provider) | minutes | API compatibility between services |

---

## Unit Tests (Domain Model)

Test the aggregate and value objects directly — no Spring context, no database. Use test fixtures and custom assertions.

**How to implement:**
- Package-private test class in the same package as the tested class
- Use `DeviceFixture` static factory methods for test data
- Use `DeviceConfigurationAssert` for fluent domain-specific assertions
- One test method per business scenario

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
}
```

**Best practices:**
- Use descriptive test method names in business language
- Structure as given/when/then — visible through fixture call / method call / assertion chain
- Test each business method independently

---

## Service Tests

Test the service layer with a fake repository implementation — no Spring, no database.

**How to implement:**
- Inline `FakeRepo` implementing the repository port with an in-memory `HashMap`
- Construct the service directly: `new DeviceService(new FakeRepo())`
- Use `DeviceFixture` for test data

```java
class DeviceServiceTest {

    final Map<String, Device> devices = new HashMap<>();
    final DeviceService service = new DeviceService(new FakeRepo());

    @Test
    void update() {
        String existingDeviceId = givenDevice();

        Optional<DeviceConfiguration> configuration = service.updateDevice(
                existingDeviceId,
                builder().openingHours(closedAtWeekend())
                        .settings(settingsWithPublicAccessAndShowOnMapOnly())
                        .build()
        );

        assertThat(configuration)
                .hasOwnership(ownership())
                .hasLocation(location())
                .hasOpeningHours(closedAtWeekend())
                .hasSettings(settingsForPublicDevice())
                .hasNoViolations();
    }

    class FakeRepo implements DeviceRepository {
        public Optional<Device> get(String deviceId) {
            return Optional.ofNullable(devices.get(deviceId));
        }
        public void save(Device device) {
            devices.put(device.deviceId, device);
        }
    }
}
```

---

## Integration Tests

Integration tests start a full Spring Boot context with Testcontainers (PostgreSQL). Use the custom `@IntegrationTest` annotation.

**How to implement:**
- Annotate with `@IntegrationTest` — composite of `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `@ActiveProfiles("integration-test")`
- `IntegrationConfiguration` starts Testcontainers PostgreSQL
- Use `@Autowired` to inject repositories, services, or projections
- Use `@Transactional` for test isolation (rollback after each test)

```java
@IntegrationTest
@Transactional
class DeviceEventSourcingRepositoryTest {

    @Autowired
    DeviceEventSourcingRepository repository;

    @Test
    void saveAndGetDevice() {
        Device saved = DeviceFixture.givenStepByStepConfiguredDevice();

        transactional(() -> repository.save(saved));
        Optional<Device> read = transactional(() -> repository.get(saved.deviceId));

        assertThat(read).isExactlyLike(saved);
    }
}
```

**Best practices:**
- `@Transactional` on test class rolls back after each test — no cleanup needed
- Use `TestTransaction.transactional()` helper when you need explicit transaction boundaries
- Use `JsonAssert` for JSON output assertions with strict or lenient mode

---

## Custom Test Infrastructure

### @IntegrationTest Composite Annotation

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles
public @interface IntegrationTest {
    @AliasFor(annotation = ActiveProfiles.class, attribute = "profiles")
    String[] profiles() default {"integration-test"};
}
```

### JsonAssert — Fluent JSON Assertion

```java
public class JsonAssert {
    public static JsonAssert assertThat(Object object) { ... }

    public JsonAssert isExactlyLike(String json) { ... }   // strict (field order + count)
    public JsonAssert hasFieldsLike(String json) { ... }   // lenient (subset of fields)
}
```

**Used for:** repository integration tests to verify JSONB serialization round-trips.

### TestTransaction — Programmatic Transaction Control

```java
public class TestTransaction {
    public static void transactional(Runnable body) { ... }
    public static <T> T transactional(Supplier<T> body) { ... }
}
```

---

## Architecture Tests (ArchUnit)

Each context has an `ArchitectureOf{Context}Test` that enforces:
- Adapters depend only on their own context + tools
- Services depend only on their own context + Spring stereotypes
- Model depends only on model + java + Jackson + validation
- Adapters are accessed only within their context
- Services are accessed only within their context or by mediators
- Model internals are accessed only within their context

See `arch-unit.md` for details.

---

## Fixture Pattern

Fixtures are public classes with static factory methods producing domain objects. One fixture per context.

```java
public class DeviceFixture {
    public static Device givenDevice() { ... }
    public static Ownership ownership() { ... }
    public static Location location() { ... }
    public static Settings settingsForPublicDevice() { ... }
}
```

**NOGO:**
- Do not use mutable shared fixture instances — always create fresh via factory methods
- Do not test domain logic only through integration tests — unit tests must cover all business rules
- Do not skip architecture tests for new contexts
