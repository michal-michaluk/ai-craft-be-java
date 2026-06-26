# Architecture Tests (ArchUnit)

ArchUnit enforces package dependency rules at compile/merge time. Each bounded context has its own `ArchitectureOf{Context}Test` that verifies the context follows the project's architecture rules.

---

## Layer Classification

The `ArchitectureDescription` utility classifies classes into three layers using naming conventions:

| Layer | Suffix | Package |
|-------|--------|---------|
| **Adapters** | `Controller`, `Repository`, `Projection`, `Entity`, `Integration`, `Listener`, `Client` | Infrastructure |
| **Services** | `Service` | Application |
| **Model** | Everything else | Domain |

```java
public class ArchitectureDescription {
    public static final DescribedPredicate<JavaClass> adapters = or(
            simpleNameEndingWith("Controller"),
            simpleNameEndingWith("Repository"),
            simpleNameEndingWith("Projection"),
            simpleNameEndingWith("Entity"),
            simpleNameEndingWith("Integration"),
            simpleNameEndingWith("Listener"),
            simpleNameEndingWith("Client")
    );
    public static final DescribedPredicate<JavaClass> services = simpleNameEndingWith("Service");
    public static final DescribedPredicate<JavaClass> model = not(services.or(adapters));
}
```

---

## Enforced Rules

### 1. Adapters Dependency Rule

Adapters may only depend on:
- Classes in their own context package
- Shared kernel types (`devices.configuration.tools`)
- Classes outside their parent package (e.g. `java..`, Spring)

```java
public static ClassesShouldConjunction adaptersDependencies(String aPackage, ...) {
    return classes()
            .that(adapters).and().resideInAPackage(aPackage)
            .should()
            .onlyDependOnClassesThat(or(
                    sharedKernelUsed,
                    resideInAnyPackage(aPackage),
                    resideInAnyPackage("devices.configuration.tools.."),
                    resideOutsideOfPackage(parentOf(aPackage) + "..")
            ));
}
```

### 2. Services Dependency Rule

Services may only depend on:
- Classes in their own context
- Spring stereotypes and annotations
- Standard Java + Lombok

### 3. Model Dependency Rule

Model classes (aggregates, value objects, events) may only depend on:
- Other model classes
- Java standard library
- Lombok
- Jackson annotations (for JSON serialization)
- Jakarta Validation (for command DTOs)

### 4. Adapters Isolation

Adapters may only be accessed by classes within their own context.

### 5. Services Isolation

Services may only be accessed by:
- Classes within their own context
- Mediators (classes in `..mediators..` packages)
- Explicitly allowed users (other contexts' exposed service ports)

### 6. Model Isolation

Types NOT in the `sharedKernelExposed` list may only be accessed within their own context.

---

## Per-Context Architecture Test

Each context defines its own `ArchitectureOf{Context}Test` with:
- `PACKAGE` — the context's package path
- `sharedKernelExposed` — types this context makes accessible to others
- `sharedKernelUsed` — types from other contexts this context may import

```java
@AnalyzeClasses(packages = "devices.configuration", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureOfDeviceContextTest {
    public static final String PACKAGE = "devices.configuration.device";

    public static final DescribedPredicate<JavaClass> sharedKernelExposed = belongToAnyOf(
            UpdateDevice.class, DeviceConfiguration.class,
            Ownership.class, Location.class
    );
    public static final DescribedPredicate<JavaClass> sharedKernelUsed = belongToAnyOf();

    @ArchTest public static final ArchRule adaptersDependencies = ArchitectureDescription.adaptersDependencies(PACKAGE, sharedKernelUsed);
    @ArchTest public static final ArchRule servicesDependencies = ArchitectureDescription.servicesDependencies(PACKAGE, sharedKernelUsed);
    @ArchTest public static final ArchRule modelDependencies = ArchitectureDescription.modelDependencies(PACKAGE, sharedKernelUsed);
    @ArchTest public static final ArchRule adaptersIsolation = ArchitectureDescription.adaptersIsolation(PACKAGE);
    @ArchTest public static final ArchRule servicesIsolation = ArchitectureDescription.servicesIsolation(PACKAGE, ArchitectureDescription.mediators);
    @ArchTest public static final ArchRule modelIsolation = ArchitectureDescription.modelIsolation(PACKAGE, sharedKernelExposed);
}
```

---

## Adding a New Bounded Context

1. Create the package `devices.configuration.{context-name}`
2. Implement classes following the naming conventions
3. Create `ArchitectureOf{Context}Test` in the test package
4. Define `sharedKernelExposed` and `sharedKernelUsed`
5. Add 6 `@ArchTest` rules using `ArchitectureDescription` helpers
6. Run the test to verify — no new rules should fail

---

## Updating `ArchitectureDescription`

When adding new adapter patterns (e.g. `Listener`, `Client`), update the `adapters` predicate in `ArchitectureDescription.java`.

**NOGO:**
- Do not weaken ArchUnit rules to make code compile — fix the architecture violation instead
- Do not skip creating architecture tests for new contexts
- Do not add unconditional `allowEmptyShould(true)` — only use when a context genuinely has no services
- Do not add new shared kernel types without updating the relevant `ArchitectureOf{Context}Test`
