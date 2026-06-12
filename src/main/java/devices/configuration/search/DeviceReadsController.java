package devices.configuration.search;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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
