package devices.configuration.intervals;

import devices.configuration.communication.BootNotification;

class BootNotificationFixture {

    static BootNotification.BootNotificationBuilder matchedDevice() {
        return BootNotification.builder()
                .deviceId("EVB-P4562137")
                .vendor("Alfen BV")
                .model("NG920-52506")
                .serial("SN-001")
                .firmware("v1.0")
                .protocol(BootNotification.Protocols.IoT16);
    }

    static BootNotification.BootNotificationBuilder defaultDevice() {
        return BootNotification.builder()
                .deviceId("unknown-001")
                .vendor("Some Vendor")
                .model("Some Model")
                .serial("SN-999")
                .firmware("v1.0")
                .protocol(BootNotification.Protocols.IoT20);
    }
}
