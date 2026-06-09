package devices.configuration.communication;

public record BootNotification(
        String deviceId,
        String protocol,
        String vendorName,
        String model,
        String serialNumber,
        String firmwareVersion
) {}
