# Adapter: HTTP Command (Write-Side Controllers)

Write-side controllers accept commands (PATCH, POST, PUT) and delegate to services. They are thin: validate input → map DTO → call service → return result.

---

## Device Controller

`DeviceController` handles device configuration updates via `PATCH /devices/{deviceId}`.

**How to implement:**

- Package-private `@RestController` with `@RequiredArgsConstructor`
- Inject the service (primary port), never the repository
- Accept `@RequestBody @Valid` command DTO
- Return the read-model snapshot (`DeviceConfiguration`)
- Throw `ResponseStatusException(HttpStatus.NOT_FOUND)` when aggregate not found

```java
@RestController
@RequiredArgsConstructor
class DeviceController {

    private final DeviceService service;

    @PatchMapping(path = "/devices/{deviceId}",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    DeviceConfiguration patchStation(@PathVariable String deviceId,
                                     @RequestBody @Valid UpdateDevice update) {
        return service.updateDevice(deviceId, update)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
```

**Best practices:**
- Controller is thin: three lines (call service, handle not-found, return)
- Command DTO (`UpdateDevice`) carries `@Valid` for Jakarta Validation
- `PATCH` semantics — null fields in the command mean "do not update"

---

## Protocol Controllers (IoT16, IoT20)

Protocol-specific controllers handle boot notifications. Each protocol has its own controller in a `protocols/{protocol-name}/` sub-package with its own request/response DTOs.

**How to implement:**

- One controller per protocol in `communication.protocols.{protocol-name}/`
- Request DTO converts to a shared domain event (`BootNotification`) via `toBootNotificationEvent(deviceId)`
- Response DTO is built from the service response using `CommunicationService.BootResponse.map()`
- URL pattern: `POST /protocols/{protocol-name}/bootnotification/{deviceId}`

```java
@RestController
@RequiredArgsConstructor
class IoT16Controller {

    private final CommunicationService service;

    @PostMapping(path = "/protocols/iot16/bootnotification/{deviceId}",
            consumes = "application/json", produces = "application/json")
    BootNotificationResponse handleBootNotification(@PathVariable String deviceId,
                                                    @RequestBody BootNotificationRequest request) {
        return service.handleBoot(request.toBootNotificationEvent(deviceId))
                .map(resp -> BootNotificationResponse.builder()
                        .currentTime(resp.serverTime().toString())
                        .interval(resp.intervalInSeconds())
                        .status(resp.state(state -> switch (state) {
                                    case UNKNOWN -> Rejected;
                                    case EXISTING -> Accepted;
                                })
                        ).build()
                );
    }
}
```

**Best practices:**
- Each protocol has its own package with its own DTOs — isolates protocol-specific formats
- Protocol-specific DTOs translate to the shared domain event (`BootNotification`)
- The shared service (`CommunicationService`) is protocol-agnostic

**NOGO:**
- Do not import protocol-specific DTOs across protocol packages
- Do not put business logic in controllers — mapping and delegation only
- Do not inject repositories directly — always go through the service

---

## Interval Rules Controller

`IntervalRulesController` provides GET/PUT for interval policy configuration.

```java
@RestController
@RequiredArgsConstructor
class IntervalRulesController {

    private final IntervalPolicyService service;

    @GetMapping("/intervals")
    IntervalRules get() {
        return service.currentRules();
    }

    @PutMapping("/intervals")
    IntervalRules update(@RequestBody IntervalRules rules) {
        service.updateRules(rules);
        return service.currentRules();
    }
}
```

---

## Command DTO Pattern

Command DTOs are `public record` with `@Builder`. They carry `@Valid` annotations on nested value objects.

```java
@Builder
public record UpdateDevice(
        @Valid Location location,
        @Valid OpeningHours openingHours,
        @Valid Settings settings,
        @Valid Ownership ownership) {

    public void apply(Device device) {
        if (location != null) device.updateLocation(location);
        if (openingHours != null) device.updateOpeningHours(openingHours);
        if (settings != null) device.updateSettings(settings);
        if (ownership != null) device.assignTo(ownership);
    }
}
```

**Best practices:**
- Null fields in command mean "do not update" — enables PATCH semantics
- The `apply(Device)` method centralizes how the command modifies the aggregate
- Command DTO is `public` because it crosses the context boundary (HTTP → service)

**NOGO:**
- Do not add business logic or validation beyond `@Valid` annotations
- Do not inject services into DTOs — they are data carriers with `apply()` only
- Do not reuse response DTOs as request DTOs or vice versa
