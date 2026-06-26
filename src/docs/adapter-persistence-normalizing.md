# Adapter: Persistence — Normalized Relational

The normalized relational adapter maps the aggregate to traditional relational tables with columns per field. It demonstrates JPA entity mapping with `@OneToMany` for sub-objects.

---

## Schema

The aggregate is split across two tables:

```
device                              opening_hours
    device_id       varchar(255) PK     id          bigint PK
    version         bigint              device_id   varchar(255) FK → device
    operator        varchar(255)        day_of_week varchar(255)
    provider        varchar(255)        open24h     boolean
    street          varchar(255)        closed      boolean
    house_number    varchar(255)        open        smallint
    city            varchar(255)        close       smallint
    postal_code     varchar(255)
    state           varchar(255)
    country         varchar(255)
    longitude       numeric(18,15)
    latitude        numeric(18,15)
    auto_start      boolean
    remote_control  boolean
    billing         boolean
    reimbursement   boolean
    show_on_map     boolean
    public_access   boolean
```

Created via Liquibase changesets `0007-device` and `0008-opening-hours`.

---

## Normalizing Repository

**How to implement:**

- `@Repository` implementing `DeviceRepository`
- Inline `NormalizedRepository` extending `CrudRepository` + `PagingAndSortingRepository`
- **Read:** Load the entity, reconstruct the aggregate from columns via `entity.getDevice()`
- **Save:** Convert aggregate to entity via `entity.setDevice(device)` using `DeviceConfiguration` snapshot
- `@OneToMany` with `orphanRemoval = true` for the opening hours sub-entity

```java
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

    @Data @Entity @Table(name = "device") @NoArgsConstructor
    static class DeviceEntity {
        @Id @Column(name = "device_id")
        private String deviceId;
        @Version private Long version;

        private String operator;
        private String provider;
        // ... location fields, settings booleans ...

        @OneToMany(orphanRemoval = true)
        @JoinColumn(name = "device_id", referencedColumnName = "device_id")
        private List<OpeningHoursEntity> openingHours;

        Device getDevice() {
            return new Device(deviceId, new ArrayList<>(),
                    new Ownership(operator, provider),
                    location(), openingHours(),
                    Settings.builder()
                            .autoStart(autoStart).remoteControl(remoteControl)
                            .billing(billing).reimbursement(reimbursement)
                            .showOnMap(showOnMap).publicAccess(publicAccess)
                            .build()
            );
        }

        void setDevice(Device device) {
            DeviceConfiguration dev = device.toDeviceConfiguration();
            this.operator = dev.ownership().operator();
            this.provider = dev.ownership().provider();
            // ... map location, settings from dev ...
            openingHours(dev.openingHours());
        }

        private void openingHours(OpeningHours openingHours) {
            if (openingHours.alwaysOpen()) {
                this.openingHours = List.of();
                return;
            }
            this.openingHours = List.of(
                    OpeningHoursEntity.of(deviceId, "monday", openingHours.opened().monday()),
                    // ... other days ...
            );
        }

        private OpeningHours openingHours() {
            if (openingHours.isEmpty()) return OpeningHours.alwaysOpened();
            var week = openingHours.stream().collect(Collectors.toUnmodifiableMap(
                    OpeningHoursEntity::getDayOfWeek, OpeningHoursEntity::toOpeningTime));
            return OpeningHours.openAt(
                    week.getOrDefault("monday", closed24h()),
                    // ... other days ...
            );
        }
    }

    @Data @Entity @Table(name = "opening_hours") @NoArgsConstructor
    static class OpeningHoursEntity {
        @Id private Long id;
        @Column(name = "device_id") private String deviceId;
        private String dayOfWeek;
        private boolean open24h;
        private boolean closed;
        private Integer open;
        private Integer close;

        static OpeningHoursEntity of(String deviceId, String dayOfWeek, OpeningTime openingTime) {
            // map OpeningTime sealed interface to columns
        }

        OpeningTime toOpeningTime() {
            if (open24h) return opened24h();
            if (closed) return closed24h();
            return opened(open, close);
        }
    }
}
```

**Best practices:**
- Aggregate → entity mapping happens entirely within the repository class
- `setDevice()` uses `DeviceConfiguration` (the snapshot) rather than accessing aggregate internals
- `@OneToMany(orphanRemoval = true)` ensures old opening hours are replaced on update
- `@Version` provides optimistic concurrency control
- The aggregate constructor is used for reconstruction — no special "from DB" path needed

**NOGO:**
- Do not map value objects directly with JPA annotations — keep JPA entities separate
- Do not expose JPA entities outside the repository class — they are implementation details
- Do not use the normalized repository for aggregates with frequently changing schemas — prefer JSONB documents
