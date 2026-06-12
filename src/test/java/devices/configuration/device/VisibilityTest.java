package devices.configuration.device;

import org.junit.jupiter.api.Test;

import static devices.configuration.device.Visibility.ForCustomer.*;
import static org.assertj.core.api.Assertions.assertThat;

class VisibilityTest {

    @Test
    void usableAndVisibleOnMap() {
        assertThat(Visibility.basedOn(true, true))
                .isEqualTo(new Visibility(true, USABLE_AND_VISIBLE_ON_MAP));
    }

    @Test
    void usableButHiddenOnMap() {
        assertThat(Visibility.basedOn(true, false))
                .isEqualTo(new Visibility(true, USABLE_BUT_HIDDEN_ON_MAP));
    }

    @Test
    void inaccessibleAndHiddenOnMap() {
        assertThat(Visibility.basedOn(false, false))
                .isEqualTo(new Visibility(false, INACCESSIBLE_AND_HIDDEN_ON_MAP));
    }

    @Test
    void inaccessibleAndHiddenOnMapEvenWhenIntendedToShow() {
        assertThat(Visibility.basedOn(false, true))
                .isEqualTo(new Visibility(false, INACCESSIBLE_AND_HIDDEN_ON_MAP));
    }
}
