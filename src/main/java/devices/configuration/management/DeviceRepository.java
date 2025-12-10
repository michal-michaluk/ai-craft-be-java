package devices.configuration.management;

import java.util.Optional;

interface DeviceRepository {
    Optional<DeviceConfigurationEditor> get(String deviceId);

    void save(DeviceConfigurationEditor device);
}
