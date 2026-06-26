# Policy (Business Calculation Rules)

Policies encapsulate business calculations and decision rules that are separate from aggregate behavior. They are stateless, composable, and independently testable.

---

## Interval Rules Policy

`IntervalRules` is a record holding configurable rules for calculating boot notification intervals. It uses a sealed interface for rule types and a chain-of-responsibility evaluation.

**How to implement:**

- Define the policy as a `record` containing rules and a calculation method
- Use a `sealed interface` for rule types with `permits` clause
- Keep the policy package-private — expose through a `@Service`
- Rules are evaluated in priority order: device-specific → model-pattern → default

```java
record IntervalRules(Duration defaultInterval, List<DeviceRule> deviceRules, List<ModelRule> modelRules) {

    Duration calculateFor(BootNotification boot) {
        for (var rule : deviceRules) {
            if (rule.matches(boot)) return rule.interval();
        }
        for (var rule : modelRules) {
            if (rule.matches(boot)) return rule.interval();
        }
        return defaultInterval;
    }

    sealed interface Rule permits DeviceRule, ModelRule {
        boolean matches(BootNotification boot);
        Duration interval();
    }

    record DeviceRule(List<String> deviceIds, Duration interval) implements Rule {
        public boolean matches(BootNotification boot) {
            return deviceIds.contains(boot.deviceId());
        }
    }

    record ModelRule(String vendor, String modelRegex, Duration interval) implements Rule {
        public boolean matches(BootNotification boot) {
            if (boot.vendor() == null || boot.model() == null) return false;
            return vendor.equals(boot.vendor()) &&
                    Pattern.matches(modelRegex, boot.model());
        }
    }
}
```

**Best practices:**
- Policy records are immutable and pure — no dependencies on services or repositories
- `calculateFor` is deterministic — same input always produces same output
- Rules are ordered: explicit (device ID) first, then pattern-based (model), then default
- Policy is persisted as a single JSONB document (single-row table `interval_rules`)

---

## Policy Service

A thin `@Service` wraps the policy record, loading it from the repository and exposing it to controllers.

```java
@Service
@Transactional
@RequiredArgsConstructor
public class IntervalPolicyService {

    private final IntervalRulesRepository repository;

    public Duration calculateFor(BootNotification boot) {
        return repository.get().calculateFor(boot);
    }

    public IntervalRules currentRules() {
        return repository.get();
    }

    public void updateRules(IntervalRules rules) {
        repository.save(rules);
    }
}
```

**Best practices:**
- The service does no calculation — it loads the record and delegates
- The service is `@Transactional` to ensure the repository save completes atomically

**NOGO:**
- Do not embed policy calculation in aggregates — policies apply to non-aggregate decisions (e.g. interval calculation)
- Do not embed policy calculation in controllers — delegate to the policy service
- Do not put mutable state in policy records — create a new record on update

---

## Policy Repository Port

Like aggregate repositories, policy repositories are package-private interfaces.

```java
interface IntervalRulesRepository {
    IntervalRules get();
    void save(IntervalRules rules);
}
```

Implemented by `IntervalRulesDocumentRepository` which stores the rules as a JSONB document in the `interval_rules` table with a fixed ID `"default"`.

---

## Unit Testing Policies

Policy tests are pure unit tests — no Spring, no database.

```java
class IntervalRulesTest {
    @Test
    void deviceSpecificHasPriority() {
        IntervalRules rules = new IntervalRules(
                Duration.ofSeconds(1800),
                List.of(new DeviceRule(List.of("device-1"), Duration.ofSeconds(3600))),
                List.of()
        );
        BootNotification boot = BootNotification.builder()
                .deviceId("device-1").build();

        Duration interval = rules.calculateFor(boot);

        assertThat(interval).isEqualTo(Duration.ofSeconds(3600));
    }
}
```

**NOGO:**
- Do not test policy rules through integration tests exclusively — unit test all rule evaluation paths
