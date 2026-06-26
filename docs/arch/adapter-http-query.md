# Adapter: HTTP Query (Read-Side Controllers)

Read-side controllers serve query endpoints that never mutate state. They are separated from write-side controllers — the CQRS principle. Queries go through a read model (projection), never through the repository port.

---

## Device Reads Controller

`DeviceReadsController` provides three query endpoints with content-negotiated response formats.

**How to implement:**

- Package-private `@RestController` in the `search` context
- Inject the projection (`ReadModelsProjection`), not the repository or service
- Read methods are `@Transactional(readOnly = true)`
- Use Spring Data `Pageable` for pagination
- Use custom `produces` media types for different response formats (`application/vnd.device.*+json`)

```java
@RestController
@RequiredArgsConstructor
class DeviceReadsController {

    private final ReadModelsProjection reads;

    @GetMapping(path = "/devices", params = {"page", "size"},
            produces = "application/vnd.device.summary+json")
    Page<DeviceSummary> getSummary(String provider, Pageable pageable) {
        return reads.findAllSummary(provider, pageable);
    }

    @GetMapping(path = "/devices", params = {"page", "size"},
            produces = "application/vnd.device.pin+json")
    List<DevicePin> getPins(String provider) {
        return reads.findAllPins(provider);
    }

    @GetMapping(path = "/devices/{deviceId}",
            produces = APPLICATION_JSON_VALUE)
    Optional<DeviceDetails> getDetails(@PathVariable String deviceId) {
        return reads.findById(deviceId);
    }
}
```

**Best practices:**
- Separate controller for reads — follows CQRS, keeps read/write concerns isolated
- Content negotiation via `produces` media type — same URL returns different formats
- Queries never emit events, never modify state
- Results come from the read model (projection), not from the write-model repository

**NOGO:**
- Do not query the write-side repository or service from read controllers
- Do not put `@Transactional` on the controller — put it on the projection method
- Do not return JPA entities directly — map to dedicated read-model records

---

## Read Model Records

Read model records are package-private and optimized for API consumption — they contain exactly what the client needs.

```java
record DeviceSummary(String deviceId, Location location, List<String> statuses) {
    static DeviceSummary ofNullable(DeviceConfiguration details, DeviceStatuses statuses) {
        if (details == null || details.location() == null) return null;
        return new DeviceSummary(
                details.deviceId(), details.location(),
                Optional.ofNullable(statuses)
                        .map(DeviceStatuses::statuses)
                        .orElse(List.of("Faulted")));
    }
}

record DevicePin(String deviceId, Location.Coordinates coordinates, List<Status> statuses) {
    enum Status { AVAILABLE, CHARGING, FAULTED }

    static DevicePin ofNullable(DeviceConfiguration details, DeviceStatuses statuses) {
        if (details == null || details.location() == null) return null;
        return new DevicePin(
                details.deviceId(), details.location().coordinates(),
                Optional.ofNullable(statuses)
                        .map(s -> s.map(DevicePin::normalised))
                        .orElse(List.of(Status.FAULTED)));
    }
}

record DeviceDetails(
        @JsonUnwrapped DeviceConfiguration configuration,
        BootNotification boot) {
}
```

**Best practices:**
- Read model records contain only fields the API needs — no full domain snapshot
- `@JsonUnwrapped` on `DeviceConfiguration` flattens the nesting for API responses
- Static factory methods (`ofNullable`) handle merging data from multiple event sources
- Lists returned are immutable — no client can mutate projection data

**NOGO:**
- Do not expose JPA entities directly as API responses
- Do not return `DeviceConfiguration` directly from query endpoints — use dedicated read model records
- Do not modify read model records after creation — keep them immutable
