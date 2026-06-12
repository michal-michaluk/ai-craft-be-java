package devices.configuration.device;

import devices.configuration.device.OpeningHours.OpeningTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static devices.configuration.device.OpeningHours.OpeningTime.*;

@Repository
@RequiredArgsConstructor
class DeviceNormalizingRepository implements DeviceRepository {

    private final NormalizedRepository repository;

    @Override
    public Optional<Device> get(String deviceId) {
        return repository.findById(deviceId)
                .map(DeviceEntity::getDevice);
    }

    @Override
    public void save(Device device) {
        DeviceEntity entity = repository.findById(device.deviceId)
                .orElseGet(() -> new DeviceEntity(device.deviceId));
        entity.setDevice(device);
        repository.save(entity);
    }

    @Repository
    interface NormalizedRepository extends CrudRepository<DeviceEntity, String>, PagingAndSortingRepository<DeviceEntity, String> {
    }

    @Data
    @Entity
    @Table(name = "device")
    @NoArgsConstructor
    static class DeviceEntity {
        @Id
        @Column(name = "device_id")
        private String deviceId;
        @Version
        private Long version;

        private String operator;
        private String provider;

        private String street;
        @Column(name = "house_number")
        private String houseNumber;
        private String city;
        @Column(name = "postal_code")
        private String postalCode;
        private String state;
        private String country;
        @Column(precision = 18, scale = 15)
        private BigDecimal longitude;
        @Column(precision = 18, scale = 15)
        private BigDecimal latitude;

        private boolean autoStart;
        @Column(name = "remote_control")
        private boolean remoteControl;
        private boolean billing;
        private boolean reimbursement;
        @Column(name = "show_on_map")
        private boolean showOnMap;
        @Column(name = "public_access")
        private boolean publicAccess;

        @OneToMany(orphanRemoval = true)
        @JoinColumn(name = "device_id", referencedColumnName = "device_id")
        private List<OpeningHoursEntity> openingHours;

        public DeviceEntity(String deviceId) {
            this.deviceId = deviceId;
        }

        Device getDevice() {
            return new Device(
                    deviceId,
                    new ArrayList<>(),
                    new Ownership(operator, provider),
                    location(),
                    openingHours(),
                    Settings.builder()
                            .autoStart(autoStart)
                            .remoteControl(remoteControl)
                            .billing(billing)
                            .reimbursement(reimbursement)
                            .showOnMap(showOnMap)
                            .publicAccess(publicAccess)
                            .build()
            );
        }

        private Location location() {
            return longitude == null ? null : new Location(
                    street,
                    houseNumber,
                    city,
                    postalCode,
                    state,
                    country,
                    new Location.Coordinates(longitude, latitude)
            );
        }

        void setDevice(Device device) {
            DeviceConfiguration dev = device.toDeviceConfiguration();
            this.operator = dev.ownership().operator();
            this.provider = dev.ownership().provider();

            this.street = dev.location() == null ? null : dev.location().street();
            this.houseNumber = dev.location() == null ? null : dev.location().houseNumber();
            this.city = dev.location() == null ? null : dev.location().city();
            this.postalCode = dev.location() == null ? null : dev.location().postalCode();
            this.state = dev.location() == null ? null : dev.location().state();
            this.country = dev.location() == null ? null : dev.location().country();
            this.longitude = dev.location() == null ? null : dev.location().coordinates().longitude();
            this.latitude = dev.location() == null ? null : dev.location().coordinates().latitude();

            this.autoStart = dev.settings().isAutoStart();
            this.remoteControl = dev.settings().isRemoteControl();
            this.billing = dev.settings().isBilling();
            this.reimbursement = dev.settings().isReimbursement();
            this.showOnMap = dev.settings().isShowOnMap();
            this.publicAccess = dev.settings().isPublicAccess();

            openingHours(dev.openingHours());
        }

        private List<OpeningHoursEntity> openingHours(OpeningHours openingHours) {
            if (openingHours.alwaysOpen()) {
                return List.of();
            }
            return List.of(
                    OpeningHoursEntity.of(deviceId, "monday", openingHours.opened().monday()),
                    OpeningHoursEntity.of(deviceId, "tuesday", openingHours.opened().tuesday()),
                    OpeningHoursEntity.of(deviceId, "wednesday", openingHours.opened().wednesday()),
                    OpeningHoursEntity.of(deviceId, "thursday", openingHours.opened().thursday()),
                    OpeningHoursEntity.of(deviceId, "friday", openingHours.opened().friday()),
                    OpeningHoursEntity.of(deviceId, "saturday", openingHours.opened().saturday()),
                    OpeningHoursEntity.of(deviceId, "sunday", openingHours.opened().sunday())
            );
        }

        private OpeningHours openingHours() {
            if (openingHours.isEmpty()) {
                return OpeningHours.alwaysOpened();
            } else {
                var week = openingHours.stream()
                        .collect(Collectors.toUnmodifiableMap(
                                OpeningHoursEntity::getDayOfWeek,
                                OpeningHoursEntity::toOpeningTime
                        ));
                return OpeningHours.openAt(
                        week.getOrDefault("monday", closed24h()),
                        week.getOrDefault("tuesday", closed24h()),
                        week.getOrDefault("wednesday", closed24h()),
                        week.getOrDefault("thursday", closed24h()),
                        week.getOrDefault("friday", closed24h()),
                        week.getOrDefault("saturday", closed24h()),
                        week.getOrDefault("sunday", closed24h())
                );
            }
        }
    }

    @Data
    @Entity
    @Table(name = "opening_hours")
    @NoArgsConstructor
    static class OpeningHoursEntity {
        @Id
        private Long id;
        @Column(name = "device_id")
        private String deviceId;
        private String dayOfWeek;
        private boolean open24h;
        private boolean closed;
        private Integer open;
        private Integer close;

        static OpeningHoursEntity of(String deviceId, String dayOfWeek, OpeningTime openingTime) {
            OpeningHoursEntity entity = new OpeningHoursEntity();
            entity.deviceId = deviceId;
            entity.dayOfWeek = dayOfWeek;
            switch (openingTime) {
                case Opened24h ignored -> {
                    entity.open24h = true;
                    entity.closed = false;
                }
                case Closed24h ignored -> {
                    entity.open24h = false;
                    entity.closed = true;
                }
                case OpenTime time -> {
                    entity.open = time.time().get(0).open().getHour();
                    entity.close = time.time().get(0).close().getHour();
                }
            }
            return entity;
        }

        OpeningTime toOpeningTime() {
            if (open24h) {
                return opened24h();
            }
            if (closed) {
                return closed24h();
            }
            return opened(open, close);
        }
    }
}
