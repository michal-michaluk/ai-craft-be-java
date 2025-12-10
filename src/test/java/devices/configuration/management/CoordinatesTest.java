package devices.configuration.management;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoordinatesTest {

    @Test
    void shouldUseValueEquality() {
        Location.Coordinates coord1 = new Location.Coordinates(10.0, 20.0);
        Location.Coordinates coord2 = new Location.Coordinates(10.0, 20.0);
        assertThat(coord1).isEqualTo(coord2);
    }

    @Test
    void shouldNotBeEqualWithDifferentValues() {
        Location.Coordinates coord1 = new Location.Coordinates(10.0, 20.0);
        Location.Coordinates coord2 = new Location.Coordinates(10.1, 20.0);
        assertThat(coord1).isNotEqualTo(coord2);
    }
}