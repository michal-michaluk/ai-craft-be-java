package devices.configuration.management;

public record Visibility(boolean isPublic, boolean isVisible) {

    public static Visibility basedOn(boolean isPublic, boolean isVisible) {
        return new Visibility(isPublic, isVisible);
    }
}
