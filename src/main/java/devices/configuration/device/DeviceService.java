package devices.configuration.device;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository repository;

    @Transactional(readOnly = true)
    public Optional<DeviceConfiguration> getDevice(String deviceId) {
        return repository.get(deviceId)
                .map(Device::toDeviceConfiguration);
    }

    public DeviceConfiguration createNewDevice(String deviceId, UpdateDevice update) {
        Device device = Device.newDevice(deviceId);
        update.apply(device);
        repository.save(device);
        return device.toDeviceConfiguration();
    }

    public Optional<DeviceConfiguration> updateDevice(String deviceId, UpdateDevice update) {
        return repository.get(deviceId)
                .map(device -> {
                    update.apply(device);
                    repository.save(device);
                    return device.toDeviceConfiguration();
                });
    }
}
