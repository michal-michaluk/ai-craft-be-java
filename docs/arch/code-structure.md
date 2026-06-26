# Code Structure

The project is a modular monolith using Domain-Driven Design with hexagonal architecture (Ports & Adapters). Code is organized by bounded context within a single Maven/Gradle module.

**Package root:** `devices.configuration`

The main class is `devices.configuration.AppRunner` with `@SpringBootApplication`.

---

## Package Layout

```
devices.configuration/
    AppRunner.java                              # @SpringBootApplication entry point
    AppConfiguration.java                       # Security, Clock, scheduling, retry config
    package-info.java

    {context-name}/                             # One package per bounded context
        {Aggregate}.java                        # Aggregate (package-private)
        {ValueObject}.java                      # Value objects (public record or package-private)
        DomainEvent.java                        # Event interface + records
        {Name}Service.java                      # Primary port / service (public @Service)
        {Name}Repository.java                   # Repository port (package-private interface)
        {Name}Controller.java                   # HTTP adapter (package-private @RestController)
        {Name}Repository.java                   # Persistence adapter (package-private @Repository)

    tools/                                      # Cross-cutting utilities
        JsonConfiguration.java                  # Jackson 3 ObjectMapper bean
        EventTypes.java                         # Event type/version mapping
        Jackson3JsonFormatMapper.java           # Hibernate FormatMapper for Jackson 3
        LastEvents.java                         # Last-event-per-type deduplication
        LegacyDomainEvent.java                  # Legacy event normalization interface
        FeatureConfiguration.java               # Generic JSONB key-value config

test/java/devices/configuration/
    IntegrationTest.java                        # @IntegrationTest composite annotation
    IntegrationConfiguration.java               # Testcontainers lifecycle
    ArchitectureDescription.java                # Reusable ArchUnit predicates and rules
    TestTransaction.java                        # Programmatic transaction helper
    JsonAssert.java                             # Fluent JSON assertion utility

    auth/
        AuthFixture.java                        # Keycloak testcontainer setup + token helper

    {context-name}/
        {Aggregate}Test.java                    # Domain unit tests
        {Name}ServiceTest.java                  # Service unit tests with fake repo
        ArchitectureOf{Context}Test.java        # ArchUnit architecture tests
        {Name}Fixture.java                      # Test data builders
        {Name}Assert.java                       # Custom AssertJ assertion
        {Name}RepositoryTest.java               # Integration test with Testcontainers
```

---

## Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Package | Lowercase, one word per context | `device`, `search`, `communication`, `intervals` |
| Aggregate | Business term, no suffix, package-private | `Device` |
| Value Object | Descriptive noun, public record | `Ownership`, `Location`, `Settings` |
| Domain Event | Past tense verb in interface, records nested inside | `DomainEvent.OwnershipUpdated` |
| Service | `{Feature}Service`, public | `DeviceService`, `CommunicationService` |
| Repository Port | `{Aggregate}Repository`, package-private | `DeviceRepository` |
| Persistence Adapter | `{Strategy}Repository`, package-private | `DeviceEventSourcingRepository` |
| Controller | `{Feature}Controller`, package-private | `DeviceController`, `DeviceReadsController` |
| Projection | `{Name}Projection`, package-private | `ReadModelsProjection`, `KnownDevicesProjection` |
| Test Fixture | `{Name}Fixture`, public | `DeviceFixture`, `CommunicationFixture` |
| Custom Assert | `{Name}Assert`, public | `DeviceConfigurationAssert` |
| Test Class | `{Target}Test`, package-private | `DeviceTest`, `DeviceServiceTest` |

---

## Bounded Contexts

| Context | Package | Purpose |
|---------|---------|---------|
| **Device** | `devices.configuration.device` | Core aggregate, value objects, command handling, 3 persistence strategies |
| **Search** | `devices.configuration.search` | Read-model projection, query endpoints, CQRS read side |
| **Communication** | `devices.configuration.communication` | Boot notification handling, protocol-specific adapters (IoT16, IoT20) |
| **Intervals** | `devices.configuration.intervals` | Boot interval policy/rules calculation |
| **Tools** | `devices.configuration.tools` | Cross-cutting: JSON, event types, feature config |

---

## Layer Classification

The `ArchitectureDescription` utility classifies all classes into three layers per context:

- **Adapters** — classes ending in `Controller`, `Repository`, `Projection`, `Entity`, `Integration`, `Listener`, `Client`
- **Services** — classes ending in `Service`
- **Model** — everything else (aggregates, value objects, domain events, commands)

Each context has its own `ArchitectureOf{Context}Test` that enforces dependency rules between these layers using ArchUnit.

---

## Key Technology Decisions

- **Java 25** with records for value objects and DTOs
- **Jackson 3** (`tools.jackson`) for JSON serialization — configured via custom `ObjectMapper` bean
- **Hibernate 6** with `@JdbcTypeCode(SqlTypes.JSON)` for JSONB columns
- **Liquibase** for schema management — `db.changelog.yaml`
- **Spring Data JPA** with `CrudRepository` + `PagingAndSortingRepository`
- **Spring Security OAuth2 Resource Server** with JWT bearer tokens
- **Lombok** for `@Builder`, `@AllArgsConstructor`, `@RequiredArgsConstructor`, `@Data`
- **Testcontainers** for PostgreSQL and Keycloak in integration tests

**NOGO:**
- Do not add new bounded context packages outside `devices.configuration.{context}`
- Do not make aggregate classes `public` — keep them package-private
- Do not create new files outside the established package-per-convention structure
- Do not add new dependencies without updating the `ArchitectureDescription` predicates
