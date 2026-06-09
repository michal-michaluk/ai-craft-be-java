package devices.configuration.communication.protocols.iot20;

import devices.configuration.communication.BootNotification;

record BootNotificationRequest(
        Device device,
        Reason reason) {

    BootNotification toBootNotification(String deviceId) {
        return new BootNotification(
                deviceId,
                "iot20",
                device.vendorName,
                device.model,
                device.serialNumber,
                device.firmwareVersion
        );
    }

    record Device(
            String serialNumber,
            String model,
            Modem modem,
            String vendorName,
            String firmwareVersion) {
    }

    record Modem(String iccid, String imsi) {
    }

    enum Reason {
        ApplicationReset,
        FirmwareUpdate,
        LocalReset,
        PowerUp,
        RemoteReset,
        ScheduledReset,
        Triggered,
        Unknown,
        Watchdog
    }
}
