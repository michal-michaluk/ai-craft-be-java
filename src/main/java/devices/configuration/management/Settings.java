package devices.configuration.management;

public record Settings(boolean autoStart, boolean remoteControl, boolean billing, boolean reimbursement,
                       boolean showOnMap, boolean publicAccess) {

    public static Settings defaultSettings() {
        return new Settings(false, false, false, false, false, false);
    }
}
