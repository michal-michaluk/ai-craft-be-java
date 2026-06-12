package devices.configuration.communication.protocols.iot20;

import devices.configuration.communication.CommunicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static devices.configuration.communication.protocols.iot20.BootNotificationResponse.Status.*;

@RestController
@RequiredArgsConstructor
class IoT20Controller {

    private final CommunicationService service;

    @PostMapping(path = "/protocols/iot20/bootnotification/{deviceId}",
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
