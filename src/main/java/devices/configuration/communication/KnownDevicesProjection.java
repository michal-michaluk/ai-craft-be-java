package devices.configuration.communication;

import devices.configuration.device.DeviceConfiguration;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static jakarta.persistence.EnumType.STRING;

@Primary
@Component
@Transactional
@AllArgsConstructor
class KnownDevicesProjection implements KnownDevices {
    private final JpaRepository repository;

    @Override
    public State get(String deviceId) {
        return repository.findById(deviceId)
                .map(KnownDeviceEntity::state)
                .orElse(State.UNKNOWN);
    }

    @EventListener
    public void handleDeInstallation(DeviceConfiguration event) {
        if (event.ownership().isUnowned()) {
            put(event.deviceId(), State.UNKNOWN);
        }
    }

    private void put(String deviceId, State state) {
        repository.save(repository.findById(deviceId)
                .orElseGet(() -> new KnownDeviceEntity(deviceId))
                .state(state)
        );
    }

    interface JpaRepository extends CrudRepository<KnownDeviceEntity, String> {
    }

    @Entity
    @Table(name = "known_device")
    @NoArgsConstructor
    static class KnownDeviceEntity {
        @Id
        private String deviceId;
        @Enumerated(STRING)
        private State state;

        public KnownDeviceEntity(String deviceId) {
            this.deviceId = deviceId;
        }

        public State state() {
            return this.state;
        }

        public KnownDeviceEntity state(State state) {
            this.state = state;
            return this;
        }
    }
}
