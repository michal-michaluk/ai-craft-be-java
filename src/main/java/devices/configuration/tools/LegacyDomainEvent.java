package devices.configuration.tools;

public interface LegacyDomainEvent {

    static <T> T normalise(T event) {
        return switch (event) {
            case LegacyDomainEvent legacy -> legacy.normalise();
            default -> event;
        };
    }

    <T> T normalise();
}
