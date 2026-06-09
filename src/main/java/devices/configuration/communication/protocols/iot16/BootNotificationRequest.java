package devices.configuration.communication.protocols.iot16;

import devices.configuration.communication.BootNotification;

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

    BootNotification toBootNotification(String deviceId) {
        return new BootNotification(
                deviceId,
                "iot16",
                chargePointVendor,
                chargePointModel,
                chargePointSerialNumber,
                firmwareVersion
        );
    }
}
