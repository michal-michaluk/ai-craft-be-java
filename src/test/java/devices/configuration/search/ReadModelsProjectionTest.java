package devices.configuration.search;

import devices.configuration.IntegrationTest;
import devices.configuration.communication.CommunicationFixture;
import devices.configuration.device.DeviceConfiguration;
import devices.configuration.device.DeviceFixture;
import devices.configuration.tools.JsonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static devices.configuration.JsonAssert.assertThat;

@IntegrationTest
@Transactional
class ReadModelsProjectionTest {

    @Autowired
    ReadModelsProjection projection;
    @Autowired
    ReadModelsProjection.DeviceReadsRepository repo;

    final String deviceId = "fixed-device-id";

    @BeforeEach
    void setUp() {
        repo.deleteAll();
    }

    @Test
    void findById() {
        givenDevice();

        Optional<DeviceDetails> read = projection.findById(deviceId);

        System.out.println(JsonConfiguration.OBJECT_MAPPER.valueToTree(read));
        assertThat(read).isExactlyLike("""
                {
                  "deviceId": "fixed-device-id",
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
                  },
                  "violations": {
                    "operatorNotAssigned": false,
                    "providerNotAssigned": false,
                    "locationMissing": false,
                    "showOnMapButMissingLocation": false,
                    "showOnMapButNoPublicAccess": false
                  },
                  "visibility": {
                    "roamingEnabled": true,
                    "forCustomer": "USABLE_BUT_HIDDEN_ON_MAP"
                  } ,
                  "boot": {
                    "deviceId": "fixed-device-id",
                    "protocol": "IoT16",
                    "vendor": "Garo",
                    "model": "CPF25 Family",
                    "serial": "820394A93203",
                    "firmware": "1.1"
                  }
                }
                """);
    }

    @Test
    void findAllPins() {
        DeviceConfiguration device = givenDevice();

        List<DevicePin> read = projection.findAllPins(device.ownership().provider());

        assertThat(read).isExactlyLike("""
                [
                  {
                    "deviceId": "fixed-device-id",
                    "coordinates": {
                      "longitude": 51.09836221719513,
                      "latitude": 16.931752852309156
                    },
                    "statuses": [
                      "AVAILABLE",
                      "FAULTED"
                    ]
                  }
                ]
                """);
    }

    @Test
    void findAllSummary() {
        DeviceConfiguration device = givenDevice();

        Page<DeviceSummary> read = projection.findAllSummary(device.ownership().provider(), Pageable.ofSize(5));

        assertThat(read).isExactlyLike("""
                {
                  "content": [
                    {
                      "deviceId": "fixed-device-id",
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
                      "statuses": [
                        "Available",
                        "Faulted"
                      ]
                    }
                  ],
                  "pageable": {
                    "sort": {
                      "orders": [],
                      "empty": true,
                      "unsorted": true,
                      "sorted": false
                    },
                    "offset": 0,
                    "pageNumber": 0,
                    "pageSize": 5,
                    "paged": true,
                    "unpaged": false
                  },
                  "total": 1,
                  "totalPages": 1,
                  "totalElements": 1,
                  "last": true,
                  "size": 5,
                  "number": 0,
                  "sort": {
                    "orders": [],
                    "empty": true,
                    "unsorted": true,
                    "sorted": false
                  },
                  "numberOfElements":1,
                  "first":true,
                  "empty":false
                }
                """);
    }

    private DeviceConfiguration givenDevice() {
        DeviceConfiguration device = DeviceFixture.givenDeviceConfiguration(deviceId);

        projection.handle(device);
        projection.handle(CommunicationFixture.boot(deviceId));
        projection.handle(CommunicationFixture.statuses(deviceId));
        return device;
    }
}
