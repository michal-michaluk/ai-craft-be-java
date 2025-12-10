package devices.configuration.management;

public record Location(
        String street,
        String houseNumber,
        String city,
        String postalCode,
        String country,
        Coordinates coordinates
) {
    public record Coordinates(double longitude, double latitude) {
    }
}
