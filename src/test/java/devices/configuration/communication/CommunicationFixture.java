package devices.configuration.communication;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static devices.configuration.communication.BootNotification.Protocols.IoT16;

public class CommunicationFixture {

    @NotNull
    public static BootNotification.BootNotificationBuilder boot() {
        return BootNotification.builder()
                .vendor("Garo")
                .model("CPF25 Family")
                .serial("820394A93203")
                .firmware("1.1")
                .protocol(IoT16);
    }

    @NotNull
    public static BootNotification boot(String deviceId) {
        return boot().deviceId(deviceId).build();
    }

    @NotNull
    public static DeviceStatuses statuses(String deviceId) {
        return new DeviceStatuses(deviceId, List.of(
                "Available",
                "Faulted"
        ));
    }
}
