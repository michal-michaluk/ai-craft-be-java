package devices.configuration.device;

import devices.configuration.tools.JsonConfiguration;
import lombok.SneakyThrows;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

public class DeviceFixture {

    public static String randomId() {
        return UUID.randomUUID().toString();
    }

    @NotNull
    public static Device givenDevice() {
        return new Device(
                randomId(),
                new ArrayList<>(),
                ownership(),
                location(),
                OpeningHours.alwaysOpened(),
                Settings.defaultSettings()
        );
    }

    @NotNull
    public static DeviceConfiguration givenDeviceConfiguration() {
        return givenDeviceConfiguration(randomId());
    }

    @NotNull
    public static DeviceConfiguration givenDeviceConfiguration(String deviceId) {
        return new DeviceConfiguration(
                deviceId,
                ownership(),
                location(),
                OpeningHours.alwaysOpened(),
                Settings.defaultSettings(),
                Violations.builder().build(),
                Visibility.basedOn(true, false)
        );
    }

    @NotNull
    public static Location location() {
        return new Location(
                "Rakietowa",
                "1A",
                "Wrocław",
                "54-621",
                null,
                "POL",
                new Location.Coordinates(new BigDecimal("51.09836221719513"), new BigDecimal("16.931752852309156")));
    }

    @NotNull
    public static Location someOtherLocation() {
        return new Location(
                "Żwirki i Wigury",
                "1H",
                "Warszawa",
                "54-202",
                null,
                "PL",
                new Location.Coordinates(
                        new BigDecimal("51.11745363251369"),
                        new BigDecimal("16.997318413019084"))
        );
    }

    @NotNull
    public static Ownership ownership() {
        return new Ownership("Devicex.nl", "public-devices");
    }

    @NotNull
    public static Ownership someOtherOwnership() {
        return new Ownership("Devicex.pl", "public-devices");
    }

    @SneakyThrows
    public static Settings settingsWithAutoStartOnly() {
        @Language("JSON") var json = """
                {
                    "autoStart": true
                }
                """;
        return JsonConfiguration.OBJECT_MAPPER.readValue(json, Settings.class);
    }

    @SneakyThrows
    public static Settings settingsWithPublicAccessAndShowOnMapOnly() {
        return Settings.builder()
                .showOnMap(true)
                .publicAccess(true)
                .build();
    }

    public static Settings settingsForPublicDevice() {
        return Settings.defaultSettings().toBuilder()
                .showOnMap(true)
                .publicAccess(true)
                .build();
    }

    public static Device givenStepByStepConfiguredDevice() {
        Device device = Device.newDevice(randomId());
        device.assignTo(ownership());
        device.updateLocation(location());
        device.updateOpeningHours(OpeningHours.alwaysOpened());
        device.updateSettings(Settings.defaultSettings());
        return device;
    }

    @NotNull
    public static OpeningHours closedAtWeekend() {
        return OpeningHours.openAt(
                OpeningHours.OpeningTime.opened24h(),
                OpeningHours.OpeningTime.opened24h(),
                OpeningHours.OpeningTime.opened24h(),
                OpeningHours.OpeningTime.opened24h(),
                OpeningHours.OpeningTime.opened24h(),
                OpeningHours.OpeningTime.closed24h(),
                OpeningHours.OpeningTime.closed24h()
        );
    }
}
