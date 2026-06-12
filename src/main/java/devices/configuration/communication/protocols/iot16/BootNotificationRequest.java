package devices.configuration.communication.protocols.iot16;

import devices.configuration.communication.BootNotification;

import static devices.configuration.communication.BootNotification.Protocols.IoT16;

record BootNotificationRequest(
        String chargePointVendor,
        String chargePointModel,
        String chargePointSerialNumber,
        String chargeBoxSerialNumber,
        String firmwareVersion,
        String iccid,
        String imsi,
        String meterType,
        String meterSerialNumber) {

    BootNotification toBootNotificationEvent(String deviceId) {
        return new BootNotification(
                deviceId, IoT16,
                chargePointVendor, chargePointModel,
                chargeBoxSerialNumber, firmwareVersion
        );
    }
}
