package devices.configuration.device;

import devices.configuration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static devices.configuration.JsonAssert.assertThat;
import static devices.configuration.TestTransaction.transactional;

@IntegrationTest
@Transactional
class DeviceEventSourcingRepositoryTest {

    @Autowired
    DeviceEventSourcingRepository repository;

    @Test
    void saveAndGetDevice() {
        Device saved = DeviceFixture.givenStepByStepConfiguredDevice();

        transactional(() -> repository.save(saved));
        Optional<Device> read = transactional(() -> repository.get(saved.deviceId));

        assertThat(read).isExactlyLike(saved);
        assertThat(read).hasFieldsLike("""
                {
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
                """);
    }
}
