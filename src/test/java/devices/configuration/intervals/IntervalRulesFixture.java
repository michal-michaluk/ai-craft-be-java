package devices.configuration.intervals;

import java.time.Duration;
import java.util.List;

class IntervalRulesFixture {

    static IntervalRules currentRules() {
        return new IntervalRules(
                Duration.ofSeconds(1800),
                List.of(
                        new IntervalRules.DeviceRule(List.of("EVB-P4562137", "ALF-9571445"), Duration.ofSeconds(600)),
                        new IntervalRules.DeviceRule(List.of("t53_8264_019", "EVB-P15079256"), Duration.ofSeconds(2700))
                ),
                List.of(
                        new IntervalRules.ModelRule("Alfen BV", "NG920-5250[6-9]", Duration.ofSeconds(60)),
                        new IntervalRules.ModelRule("ChargeStorm AB", "Chargestorm Connected", Duration.ofSeconds(120))
                )
        );
    }

    static IntervalRules emptyRules() {
        return new IntervalRules(
                Duration.ofSeconds(1800),
                List.of(),
                List.of()
        );
    }
}
