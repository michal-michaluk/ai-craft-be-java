package devices.configuration.device;

import java.util.Optional;

interface DeviceRepository {

    Optional<Device> get(String deviceId);

    void save(Device device);
}
