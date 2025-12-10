package devices.configuration.management;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ViolationsTest {

    @Test
    void shouldBuildViolationsWithBuilder() {
        Violations violations = Violations.builder()
                .operatorNotAssigned(true)
                .providerNotAssigned(false)
                .locationMissing(true)
                .showOnMapButMissingLocation(false)
                .showOnMapButNoPublicAccess(false)
                .build();

        assertThat(violations.operatorNotAssigned()).isTrue();
        assertThat(violations.providerNotAssigned()).isFalse();
        assertThat(violations.locationMissing()).isTrue();
        assertThat(violations.showOnMapButMissingLocation()).isFalse();
        assertThat(violations.showOnMapButNoPublicAccess()).isFalse();
    }

    @Test
    void shouldBeValidWhenNoViolations() {
        Violations violations = Violations.builder()
                .operatorNotAssigned(false)
                .providerNotAssigned(false)
                .locationMissing(false)
                .showOnMapButMissingLocation(false)
                .showOnMapButNoPublicAccess(false)
                .build();

        assertThat(violations.isValid()).isTrue();
    }

    @Test
    void shouldNotBeValidWhenHasViolations() {
        Violations violations = Violations.builder()
                .operatorNotAssigned(true)
                .providerNotAssigned(false)
                .locationMissing(false)
                .showOnMapButMissingLocation(false)
                .showOnMapButNoPublicAccess(false)
                .build();

        assertThat(violations.isValid()).isFalse();
    }
}