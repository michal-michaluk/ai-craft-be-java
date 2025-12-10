package devices.configuration.management;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocationTest {

    @Test
    void shouldUseValueEquality() {
        Location.Coordinates coords = new Location.Coordinates(10.0, 20.0);
        Location location1 = new Location("street", "1A", "city", "12345", "country", coords);
        Location location2 = new Location("street", "1A", "city", "12345", "country", coords);
        assertThat(location1).isEqualTo(location2);
    }

    @Test
    void shouldNotBeEqualWithDifferentValues() {
        Location.Coordinates coords = new Location.Coordinates(10.0, 20.0);
        Location location1 = new Location("street", "1A", "city", "12345", "country", coords);
        Location location2 = new Location("street2", "1A", "city", "12345", "country", coords);
        assertThat(location1).isNotEqualTo(location2);
    }
}