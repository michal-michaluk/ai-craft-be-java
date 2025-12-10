package devices.configuration.management;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OwnershipTest {

    @Test
    void shouldBeUnownedWhenBothNull() {
        Ownership ownership = Ownership.unowned();
        assertThat(ownership.isUnowned()).isTrue();
        assertThat(ownership.isOwned()).isFalse();
    }

    @Test
    void shouldBeOwnedWhenBothPresent() {
        Ownership ownership = Ownership.of("operator1", "provider1");
        assertThat(ownership.isOwned()).isTrue();
        assertThat(ownership.isUnowned()).isFalse();
    }

    @Test
    void shouldUseValueEquality() {
        Ownership ownership1 = Ownership.of("operator1", "provider1");
        Ownership ownership2 = Ownership.of("operator1", "provider1");
        assertThat(ownership1).isEqualTo(ownership2);
    }

    @Test
    void shouldThrowWhenPartiallyNull() {
        assertThatThrownBy(() -> new Ownership("operator", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ownership must be either unowned (both null) or owned (both set)");
    }

    @Test
    void shouldAllowCreationWithBothNull() {
        Ownership ownership = new Ownership(null, null);
        assertThat(ownership.isUnowned()).isTrue();
    }
}