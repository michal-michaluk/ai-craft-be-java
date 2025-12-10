package devices.configuration.management;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
class KafkaPublisher {

    private final KafkaTemplate<String, DeviceConfigurationSnapshot> kafkaTemplate;

    @EventListener
    public void onDeviceConfiguration(DeviceConfiguration event) {
        var snapshot = DeviceConfigurationSnapshot.from(event);
        kafkaTemplate.send("device-configuration-snapshot-v1", event.deviceId(), snapshot);
    }

    record DeviceConfigurationSnapshot(
            String deviceId,
            String operator,
            String provider,
            String street,
            String houseNumber,
            String city,
            String postalCode,
            String country,
            Double longitude,
            Double latitude,
            Boolean alwaysOpen,
            Boolean autoStart,
            Boolean remoteControl,
            Boolean billing,
            Boolean reimbursement,
            Boolean showOnMap,
            Boolean publicAccess,
            Boolean operatorNotAssigned,
            Boolean providerNotAssigned,
            Boolean locationMissing,
            Boolean showOnMapButMissingLocation,
            Boolean showOnMapButNoPublicAccess,
            Boolean isPublic,
            Boolean isVisible
    ) {


        static DeviceConfigurationSnapshot from(DeviceConfiguration config) {
            return new DeviceConfigurationSnapshot(
                    config.deviceId(),
                    config.ownership() != null ? config.ownership().operator() : null,
                    config.ownership() != null ? config.ownership().provider() : null,
                    config.location() != null ? config.location().street() : null,
                    config.location() != null ? config.location().houseNumber() : null,
                    config.location() != null ? config.location().city() : null,
                    config.location() != null ? config.location().postalCode() : null,
                    config.location() != null ? config.location().country() : null,
                    config.location() != null && config.location().coordinates() != null ? config.location().coordinates().longitude() : null,
                    config.location() != null && config.location().coordinates() != null ? config.location().coordinates().latitude() : null,
                    config.openingHours() != null ? config.openingHours().alwaysOpen() : null,
                    config.settings() != null ? config.settings().autoStart() : null,
                    config.settings() != null ? config.settings().remoteControl() : null,
                    config.settings() != null ? config.settings().billing() : null,
                    config.settings() != null ? config.settings().reimbursement() : null,
                    config.settings() != null ? config.settings().showOnMap() : null,
                    config.settings() != null ? config.settings().publicAccess() : null,
                    config.violations() != null ? config.violations().operatorNotAssigned() : null,
                    config.violations() != null ? config.violations().providerNotAssigned() : null,
                    config.violations() != null ? config.violations().locationMissing() : null,
                    config.violations() != null ? config.violations().showOnMapButMissingLocation() : null,
                    config.violations() != null ? config.violations().showOnMapButNoPublicAccess() : null,
                    config.visibility() != null ? config.visibility().isPublic() : null,
                    config.visibility() != null ? config.visibility().isVisible() : null
            );
        }
    }
}
