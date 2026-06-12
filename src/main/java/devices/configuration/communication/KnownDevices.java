package devices.configuration.communication;

public interface KnownDevices {
    enum State {UNKNOWN, EXISTING}

    State get(String deviceId);
}
