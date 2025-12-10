package devices.configuration.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
class InstallationFinishedListener {

    private final DeviceService deviceService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "installations-v1", groupId = "device-service")
    public void handleInstallationEvent(String message) {
        try {
            InstallationFinishedEvent event = objectMapper.readValue(message, InstallationFinishedEvent.class);
            if ("InstallationFinished".equals(event.type())) {
                UpdateDevice update = UpdateDevice.of(
                        event.ownership().toDomain(),
                        event.location().toDomain()
                );
                deviceService.createDevice(event.deviceId(), update);
            }
        } catch (Exception e) {
            log.error("Error processing installation event: {}", e.getMessage(), e);
        }
    }

    record InstallationFinishedEvent(
            String type,
            String deviceId,
            OwnershipTransport ownership,
            LocationTransport location
    ) {
    }

    record OwnershipTransport(String operator, String provider) {
        Ownership toDomain() {
            return new Ownership(operator, provider);
        }
    }

    record LocationTransport(
            String street,
            String houseNumber,
            String city,
            String postalCode,
            String country,
            CoordinatesTransport coordinates
    ) {
        Location toDomain() {
            return new Location(street,
                    houseNumber,
                    city,
                    postalCode,
                    country,
                    coordinates.toDomain());
        }
    }

    record CoordinatesTransport(double longitude, double latitude) {
        Location.Coordinates toDomain() {
            return new Location.Coordinates(longitude, latitude);
        }
    }
}
