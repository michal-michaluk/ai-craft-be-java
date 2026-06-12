package devices.configuration.search;

import devices.configuration.communication.BootNotification;
import devices.configuration.communication.DeviceStatuses;
import devices.configuration.device.DeviceConfiguration;
import devices.configuration.device.Ownership;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@Transactional
@AllArgsConstructor
class ReadModelsProjection {

    private final DeviceReadsRepository repository;

    @EventListener
    public void handle(DeviceConfiguration details) {
        DeviceReadsEntity entity = repository.findById(details.deviceId())
                .orElseGet(() -> new DeviceReadsEntity(details.deviceId()));

        entity
                .setOwnership(details.ownership())
                .setDetails(details)
                .setPin(DevicePin.ofNullable(details, entity.getStatuses()))
                .setSummary(DeviceSummary.ofNullable(details, entity.getStatuses()));

        repository.save(entity);
    }

    @EventListener
    public void handle(BootNotification boot) {
        DeviceReadsEntity entity = repository.findById(boot.deviceId())
                .orElseGet(() -> new DeviceReadsEntity(boot.deviceId()));

        entity.setBoot(boot);

        repository.save(entity);
    }

    @EventListener
    public void handle(DeviceStatuses statuses) {
        DeviceReadsEntity entity = repository.findById(statuses.deviceId())
                .orElseGet(() -> new DeviceReadsEntity(statuses.deviceId()));

        entity
                .setStatuses(statuses)
                .setPin(DevicePin.ofNullable(entity.getDetails(), statuses))
                .setSummary(DeviceSummary.ofNullable(entity.getDetails(), statuses));

        repository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<DeviceDetails> findById(String deviceId) {
        return repository.findById(deviceId)
                .map(entity -> new DeviceDetails(entity.details, entity.boot));
    }

    @Transactional(readOnly = true)
    public List<DevicePin> findAllPins(String provider) {
        return repository.findAllByProvider(provider)
                .map(DeviceReadsEntity::getPin)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<DeviceSummary> findAllSummary(String provider, Pageable pageable) {
        return repository.findAllByProvider(provider, pageable)
                .map(DeviceReadsEntity::getSummary);
    }

    @Repository
    interface DeviceReadsRepository extends CrudRepository<DeviceReadsEntity, String>, PagingAndSortingRepository<DeviceReadsEntity, String> {
        Stream<DeviceReadsEntity> findAllByProvider(String provider);

        Page<DeviceReadsEntity> findAllByProvider(String provider, Pageable pageable);
    }

    @Data
    @Accessors(chain = true)
    @Entity
    @DynamicUpdate
    @Table(name = "device_reads")
    @NoArgsConstructor
    static class DeviceReadsEntity {

        @Id
        private String deviceId;
        @Version
        private Long version;
        private String operator;
        private String provider;

        @JdbcTypeCode(SqlTypes.JSON)
        private DevicePin pin;

        @JdbcTypeCode(SqlTypes.JSON)
        private DeviceSummary summary;

        @JdbcTypeCode(SqlTypes.JSON)
        private DeviceConfiguration details;

        @JdbcTypeCode(SqlTypes.JSON)
        private DeviceStatuses statuses;
        @JdbcTypeCode(SqlTypes.JSON)
        private BootNotification boot;

        DeviceReadsEntity(String deviceId) {
            this.deviceId = deviceId;
        }

        DeviceReadsEntity setOwnership(Ownership ownership) {
            operator = ownership.operator();
            provider = ownership.provider();
            return this;
        }
    }

}
