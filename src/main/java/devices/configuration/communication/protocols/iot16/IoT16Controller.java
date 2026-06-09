package devices.configuration.communication.protocols.iot16;

import devices.configuration.communication.CommunicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static devices.configuration.communication.protocols.iot16.BootNotificationResponse.Status.Accepted;

@RestController
@RequiredArgsConstructor
class IoT16Controller {

    private final CommunicationService communicationService;

    @PostMapping(path = "/protocols/iot16/bootnotification/{deviceId}",
            consumes = "application/json", produces = "application/json")
    BootNotificationResponse handleBootNotification(@PathVariable String deviceId,
                                                    @RequestBody BootNotificationRequest request) {
        var response = communicationService.handleBoot(deviceId, request.toBootNotification(deviceId));
        return BootNotificationResponse.builder()
                .currentTime(response.currentTime().toString())
                .interval(response.intervalSeconds())
                .status(Accepted)
                .build();
    }

}
