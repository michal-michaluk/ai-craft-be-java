package devices.configuration.management;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpeningHoursTest {

    @Test
    void shouldCreateAlwaysOpened() {
        OpeningHours hours = OpeningHours.alwaysOpened();
        assertThat(hours.alwaysOpen()).isTrue();
    }

    @Test
    void shouldUseValueEquality() {
        OpeningHours hours1 = new OpeningHours(true);
        OpeningHours hours2 = new OpeningHours(true);
        assertThat(hours1).isEqualTo(hours2);
    }

    @Test
    void shouldNotBeEqualWithDifferentValues() {
        OpeningHours hours1 = new OpeningHours(true);
        OpeningHours hours2 = new OpeningHours(false);
        assertThat(hours1).isNotEqualTo(hours2);
    }
}