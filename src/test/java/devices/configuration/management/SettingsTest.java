package devices.configuration.management;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SettingsTest {

    @Test
    void shouldCreateDefaultSettings() {
        Settings settings = Settings.defaultSettings();
        assertThat(settings.autoStart()).isFalse();
        assertThat(settings.remoteControl()).isFalse();
        assertThat(settings.billing()).isFalse();
        assertThat(settings.reimbursement()).isFalse();
        assertThat(settings.showOnMap()).isFalse();
        assertThat(settings.publicAccess()).isFalse();
    }

    @Test
    void shouldUseValueEquality() {
        Settings settings1 = new Settings(false, false, false, false, false, false);
        Settings settings2 = new Settings(false, false, false, false, false, false);
        assertThat(settings1).isEqualTo(settings2);
    }

    @Test
    void shouldNotBeEqualWithDifferentValues() {
        Settings settings1 = new Settings(false, false, false, false, false, false);
        Settings settings2 = new Settings(true, false, false, false, false, false);
        assertThat(settings1).isNotEqualTo(settings2);
    }
}