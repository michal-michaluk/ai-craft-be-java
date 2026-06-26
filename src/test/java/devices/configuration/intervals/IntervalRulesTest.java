package devices.configuration.intervals;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static devices.configuration.intervals.BootNotificationFixture.*;
import static devices.configuration.intervals.IntervalRulesFixture.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IntervalRulesTest {

    IntervalRules rules = currentRules();

    @Test
    void deviceRuleMatchFirstGroup() {
        Duration interval = rules.calculateFor(matchedDevice().build());

        assertEquals(Duration.ofSeconds(600), interval);
    }

    @Test
    void deviceRuleMatchFirstGroupSecondDevice() {
        Duration interval = rules.calculateFor(matchedDevice()
                .deviceId("ALF-9571445").build());

        assertEquals(Duration.ofSeconds(600), interval);
    }

    @Test
    void deviceRuleMatchSecondGroupFirstDevice() {
        Duration interval = rules.calculateFor(matchedDevice()
                .deviceId("t53_8264_019").build());

        assertEquals(Duration.ofSeconds(2700), interval);
    }

    @Test
    void deviceRuleMatchSecondGroupSecondDevice() {
        Duration interval = rules.calculateFor(matchedDevice()
                .deviceId("EVB-P15079256").build());

        assertEquals(Duration.ofSeconds(2700), interval);
    }

    @Test
    void deviceWinsOverModel() {
        Duration interval = rules.calculateFor(defaultDevice()
                .deviceId("EVB-P4562137")
                .vendor("Alfen BV")
                .model("NG920-52509").build());

        assertEquals(Duration.ofSeconds(600), interval);
    }

    @Test
    void modelRuleMatchViaRegex() {
        Duration interval = rules.calculateFor(defaultDevice()
                .deviceId("any")
                .vendor("Alfen BV")
                .model("NG920-52509").build());

        assertEquals(Duration.ofSeconds(60), interval);
    }

    @Test
    void modelNoMatchOutsideRegexRange() {
        Duration interval = rules.calculateFor(defaultDevice()
                .deviceId("any")
                .vendor("Alfen BV")
                .model("NG920-52505").build());

        assertEquals(Duration.ofSeconds(1800), interval);
    }

    @Test
    void modelRuleExactMatch() {
        Duration interval = rules.calculateFor(defaultDevice()
                .deviceId("any")
                .vendor("ChargeStorm AB")
                .model("Chargestorm Connected").build());

        assertEquals(Duration.ofSeconds(120), interval);
    }

    @Test
    void defaultFallback() {
        Duration interval = rules.calculateFor(defaultDevice().build());

        assertEquals(Duration.ofSeconds(1800), interval);
    }

    @Test
    void nullVendorAndModelDoNotCrash() {
        Duration interval = rules.calculateFor(defaultDevice()
                .vendor(null)
                .model(null).build());

        assertEquals(Duration.ofSeconds(1800), interval);
    }
}
