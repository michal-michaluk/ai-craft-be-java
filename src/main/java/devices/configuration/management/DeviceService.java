package devices.configuration.management;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeviceService {

    private final DeviceRepository repository;

    @Transactional(readOnly = true)
    public Optional<DeviceConfiguration> getDevice(String deviceId) {
        return repository.get(deviceId).map(DeviceConfigurationEditor::toDeviceConfiguration);
    }

    public DeviceConfiguration createDevice(String deviceId) {
        DeviceConfigurationEditor device = DeviceConfigurationEditor.createNewDevice(deviceId);
        repository.save(device);
        return device.toDeviceConfiguration();
    }

    public DeviceConfiguration updateDevice(String deviceId, UpdateDevice update) {
        DeviceConfigurationEditor device = repository.get(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));
        update.apply(device);
        repository.save(device);
        return device.toDeviceConfiguration();
    }
}