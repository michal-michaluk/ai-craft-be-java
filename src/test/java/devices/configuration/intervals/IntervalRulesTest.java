package devices.configuration.intervals;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntervalRulesTest {

    @Test
    void shouldMatchDeviceIdRule() {
        var rule = new IntervalRule.DeviceIdRule("EVB-P4562137", Duration.ofSeconds(600));

        assertThat(rule.matches("EVB-P4562137", "any", "any")).isTrue();
        assertThat(rule.matches("unknown", "any", "any")).isFalse();
    }

    @Test
    void shouldRejectBlankDeviceId() {
        assertThatThrownBy(() -> new IntervalRule.DeviceIdRule(" ", Duration.ofSeconds(600)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldMatchModelRuleByVendorAndRegex() {
        var rule = new IntervalRule.ModelRule("Alfen BV", Pattern.compile("NG920-5250[6-9]"), Duration.ofSeconds(60));

        assertThat(rule.matches("any", "Alfen BV", "NG920-52507")).isTrue();
        assertThat(rule.matches("any", "Alfen BV", "NG920-52505")).isFalse();
        assertThat(rule.matches("any", "Other Vendor", "NG920-52507")).isFalse();
    }

    @Test
    void shouldRejectBlankVendor() {
        assertThatThrownBy(() -> new IntervalRule.ModelRule("", Pattern.compile(".*"), Duration.ofSeconds(60)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldEvaluateDeviceIdRuleFirst() {
        var rules = new IntervalRules(
                List.of(new IntervalRule.DeviceIdRule("DEVICE-001", Duration.ofSeconds(999))),
                List.of(new IntervalRule.ModelRule("Any", Pattern.compile(".*"), Duration.ofSeconds(111))),
                Duration.ofSeconds(1800)
        );

        assertThat(rules.evaluate("DEVICE-001", "Any", "anything"))
                .isEqualTo(Duration.ofSeconds(999));
    }

    @Test
    void shouldEvaluateModelRuleWhenNoDeviceIdMatch() {
        var rules = new IntervalRules(
                List.of(),
                List.of(new IntervalRule.ModelRule("Garo", Pattern.compile("CPF25.*"), Duration.ofSeconds(120))),
                Duration.ofSeconds(1800)
        );

        assertThat(rules.evaluate("unknown", "Garo", "CPF25 Family"))
                .isEqualTo(Duration.ofSeconds(120));
    }

    @Test
    void shouldFallbackToDefaultWhenNoRuleMatches() {
        var rules = IntervalRules.withDefaults();

        assertThat(rules.evaluate("unknown", "unknown", "unknown"))
                .isEqualTo(Duration.ofSeconds(1800));
    }

    @Test
    void shouldUseSpecExample() {
        var rules = new IntervalRules(
                List.of(
                        new IntervalRule.DeviceIdRule("EVB-P4562137", Duration.ofSeconds(600)),
                        new IntervalRule.DeviceIdRule("ALF-9571445", Duration.ofSeconds(600)),
                        new IntervalRule.DeviceIdRule("t53_8264_019", Duration.ofSeconds(2700)),
                        new IntervalRule.DeviceIdRule("EVB-P15079256", Duration.ofSeconds(2700))
                ),
                List.of(
                        new IntervalRule.ModelRule("Alfen BV", Pattern.compile("NG920-5250[6-9]"), Duration.ofSeconds(60)),
                        new IntervalRule.ModelRule("ChargeStorm AB", Pattern.compile("Chargestorm Connected"), Duration.ofSeconds(120))
                ),
                Duration.ofSeconds(1800)
        );

        assertThat(rules.evaluate("EVB-P4562137", "any", "any")).isEqualTo(Duration.ofSeconds(600));
        assertThat(rules.evaluate("unknown-id", "Alfen BV", "NG920-52507")).isEqualTo(Duration.ofSeconds(60));
        assertThat(rules.evaluate("unknown-id", "unknown", "unknown")).isEqualTo(Duration.ofSeconds(1800));
    }

    @Test
    void shouldCreateImmutableCopies() {
        var deviceRules = new java.util.ArrayList<>(List.of(
                new IntervalRule.DeviceIdRule("d1", Duration.ofSeconds(1))
        ));
        var modelRules = new java.util.ArrayList<>(List.of(
                new IntervalRule.ModelRule("v", Pattern.compile(".*"), Duration.ofSeconds(2))
        ));
        var rules = new IntervalRules(deviceRules, modelRules, Duration.ofSeconds(1800));

        deviceRules.clear();
        modelRules.clear();

        assertThat(rules.evaluate("d1", "v", "m")).isEqualTo(Duration.ofSeconds(1));
    }
}
