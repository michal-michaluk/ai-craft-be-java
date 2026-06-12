package devices.configuration.device;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static devices.configuration.device.DeviceConfigurationAssert.assertThat;
import static devices.configuration.device.DeviceFixture.*;
import static devices.configuration.device.UpdateDevice.builder;

class DeviceServiceTest {

    final Map<String, Device> devices = new HashMap<>();
    final DeviceService service = new DeviceService(new FakeRepo());

    @Test
    void getDeviceConfiguration() {
        String deviceId = givenDevice();
        Optional<DeviceConfiguration> configuration = service.getDevice(deviceId);

        assertThat(configuration)
                .hasOwnership(ownership())
                .hasLocation(location())
                .hasOpeningHours(OpeningHours.alwaysOpened())
                .hasSettings(Settings.defaultSettings())
                .hasNoViolations()
                .isNotVisible();
    }

    @Test
    void getUnknownDeviceConfiguration() {
        Optional<DeviceConfiguration> configuration = service.getDevice("fake-device-id");

        Assertions.assertThat(configuration).isEmpty();
    }

    @Test
    void createNewDevice() {
        DeviceConfiguration configuration = service.createNewDevice(
                "new-device-id",
                UpdateDevice.use(ownership(), location())
        );

        assertThat(configuration)
                .hasOwnership(ownership())
                .hasLocation(location())
                .hasOpeningHours(OpeningHours.alwaysOpened())
                .hasSettings(Settings.defaultSettings())
                .hasNoViolations()
                .isNotVisible()
        ;

    }

    @Test
    void recreateDeviceWithExistingId() {
        String existingDeviceId = givenDevice();

        DeviceConfiguration configuration = service.createNewDevice(
                existingDeviceId,
                UpdateDevice.use(someOtherOwnership(), someOtherLocation())
        );

        assertThat(configuration)
                .hasOwnership(someOtherOwnership())
                .hasLocation(someOtherLocation())
                .hasOpeningHours(OpeningHours.alwaysOpened())
                .hasSettings(Settings.defaultSettings())
                .hasNoViolations();
    }

    @Test
    void update() {
        String existingDeviceId = givenDevice();

        Optional<DeviceConfiguration> configuration = service.updateDevice(
                existingDeviceId,
                builder()
                        .openingHours(closedAtWeekend())
                        .settings(settingsWithPublicAccessAndShowOnMapOnly())
                        .build()
        );

        assertThat(configuration)
                .hasOwnership(ownership())
                .hasLocation(location())
                .hasOpeningHours(closedAtWeekend())
                .hasSettings(settingsForPublicDevice())
                .hasNoViolations();
    }


    private String givenDevice() {
        Device device = DeviceFixture.givenDevice();
        devices.put(device.deviceId, device);
        return device.deviceId;
    }

    class FakeRepo implements DeviceRepository {
        @Override
        public Optional<Device> get(String deviceId) {
            return Optional.ofNullable(devices.get(deviceId));
        }

        @Override
        public void save(Device device) {
            devices.put(device.deviceId, device);
        }
    }
}
