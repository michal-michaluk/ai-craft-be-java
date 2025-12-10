package devices.configuration.management;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VisibilityTest {

    @Test
    void shouldCreateVisibilityBasedOnParams() {
        Visibility visibility = Visibility.basedOn(true, false);
        assertThat(visibility.isPublic()).isTrue();
        assertThat(visibility.isVisible()).isFalse();
    }

    @Test
    void shouldUseValueEquality() {
        Visibility visibility1 = new Visibility(true, false);
        Visibility visibility2 = new Visibility(true, false);
        assertThat(visibility1).isEqualTo(visibility2);
    }
}