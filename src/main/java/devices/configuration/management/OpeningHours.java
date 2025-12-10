package devices.configuration.management;

public record OpeningHours(boolean alwaysOpen) {

    public static OpeningHours alwaysOpened() {
        return new OpeningHours(true);
    }
}
