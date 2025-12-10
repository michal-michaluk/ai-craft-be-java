package devices.configuration.management;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
class DeviceController {

    private final DeviceService service;

    @GetMapping(path = "/devices/{deviceId}", produces = APPLICATION_JSON_VALUE)
    DeviceConfiguration get(@PathVariable String deviceId) {
        return service.getDevice(deviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping(path = "/devices/{deviceId}",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    DeviceConfiguration post(@PathVariable String deviceId,
                             @RequestBody UpdateDevice update) {
        return service.createDevice(deviceId, update);
    }

    @PatchMapping(path = "/devices/{deviceId}",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    DeviceConfiguration patch(@PathVariable String deviceId,
                              @RequestBody UpdateDevice update) {
        return service.updateDevice(deviceId, update);
    }
}
