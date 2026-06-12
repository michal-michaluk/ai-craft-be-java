package devices.configuration.device;

import devices.configuration.IntegrationTest;
import devices.configuration.JsonAssert;
import devices.configuration.device.DomainEvent.LocationUpdated;
import devices.configuration.device.DomainEvent.OwnershipUpdated;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static devices.configuration.TestTransaction.transactional;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@Transactional
@RecordApplicationEvents
class DeviceDocumentWithHistoryRepositoryTest {

    @Autowired
    DeviceDocumentWithHistoryRepository repository;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private ApplicationEvents emitted;

    @Test
    void saveAndGetDevice() {
        Device saved = DeviceFixture.givenStepByStepConfiguredDevice();
        transactional(() -> repository.save(saved));
        Optional<Device> read = transactional(() -> repository.get(saved.deviceId));

        JsonAssert.assertThat(read).isExactlyLike("""
                {
                  "deviceId": "%s",
                  "events": [],
                  "ownership": {
                    "operator": "Devicex.nl",
                    "provider": "public-devices"
                  },
                  "location": {
                    "street": "Rakietowa",
                    "houseNumber": "1A",
                    "city": "Wrocław",
                    "postalCode": "54-621",
                    "state": null,
                    "country": "POL",
                    "coordinates": {
                      "longitude": 51.09836221719513,
                      "latitude": 16.931752852309156
                    }
                  },
                  "openingHours": {
                    "alwaysOpen": true
                  },
                  "settings": {
                    "autoStart": false,
                    "remoteControl": false,
                    "billing": false,
                    "reimbursement": false,
                    "showOnMap": false,
                    "publicAccess": false
                  }
                }
                """, saved.deviceId);
    }

    @Test
    void emitsDomainEvents() {
        Device saved = DeviceFixture.givenStepByStepConfiguredDevice();
        transactional(() -> repository.save(saved));

        assertThat(emitted.stream(DomainEvent.class))
                .containsExactly(
                        new OwnershipUpdated(saved.deviceId, DeviceFixture.ownership()),
                        new LocationUpdated(saved.deviceId, DeviceFixture.location())
                );
        assertThat(emitted.stream(DeviceConfiguration.class))
                .containsExactly(saved.toDeviceConfiguration());
    }
}
