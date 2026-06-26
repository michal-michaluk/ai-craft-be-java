package devices.configuration.intervals;

import devices.configuration.tools.JsonConfiguration;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static devices.configuration.JsonAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IntervalRulesJsonCompatTest {

    @Test
    void deserializeDefaultRules() throws Exception {
        var json = """
                {"defaultInterval": 1800, "deviceRules": [], "modelRules": []}
                """;

        var rules = JsonConfiguration.OBJECT_MAPPER.readValue(json, IntervalRules.class);

        assertEquals(Duration.ofSeconds(1800), rules.defaultInterval());
        assertEquals(List.of(), rules.deviceRules());
        assertEquals(List.of(), rules.modelRules());
    }

    @Test
    void deserializeFullRules() throws Exception {
        var json = """
                {
                  "defaultInterval": 1800,
                  "deviceRules": [
                    {
                      "deviceIds": ["EVB-P4562137", "ALF-9571445"],
                      "interval": 600
                    },
                    {
                      "deviceIds": ["t53_8264_019", "EVB-P15079256"],
                      "interval": 2700
                    }
                  ],
                  "modelRules": [
                    {
                      "vendor": "Alfen BV",
                      "modelRegex": "NG920-5250[6-9]",
                      "interval": 60
                    },
                    {
                      "vendor": "ChargeStorm AB",
                      "modelRegex": "Chargestorm Connected",
                      "interval": 120
                    }
                  ]
                }
                """;

        var rules = JsonConfiguration.OBJECT_MAPPER.readValue(json, IntervalRules.class);

        assertEquals(Duration.ofSeconds(1800), rules.defaultInterval());
        assertEquals(2, rules.deviceRules().size());
        assertEquals(2, rules.modelRules().size());
        assertEquals("EVB-P4562137", rules.deviceRules().get(0).deviceIds().get(0));
        assertEquals("t53_8264_019", rules.deviceRules().get(1).deviceIds().get(0));
        assertEquals("Alfen BV", rules.modelRules().get(0).vendor());
    }

    @Test
    void roundtripJson() throws Exception {
        var original = new IntervalRules(
                Duration.ofSeconds(2700),
                List.of(
                        new IntervalRules.DeviceRule(List.of("t53_8264_019"), Duration.ofSeconds(2700))
                ),
                List.of(
                        new IntervalRules.ModelRule("ChargeStorm AB", "Chargestorm Connected", Duration.ofSeconds(120))
                )
        );

        var json = JsonConfiguration.OBJECT_MAPPER.writeValueAsString(original);
        var restored = JsonConfiguration.OBJECT_MAPPER.readValue(json, IntervalRules.class);

        assertThat(restored).isExactlyLike(original);
    }

    @Test
    void durationSerializedAsSeconds() throws Exception {
        var rules = new IntervalRules(
                Duration.ofSeconds(120),
                List.of(new IntervalRules.DeviceRule(List.of("X"), Duration.ofSeconds(30))),
                List.of(new IntervalRules.ModelRule("V", "M", Duration.ofSeconds(45)))
        );

        var json = JsonConfiguration.OBJECT_MAPPER.writeValueAsString(rules);

        assertThat(json).isExactlyLike("""
                {
                  "defaultInterval": 120,
                  "deviceRules": [{"deviceIds": ["X"], "interval": 30}],
                  "modelRules": [{"vendor": "V", "modelRegex": "M", "interval": 45}]
                }
                """);
    }

    @Test
    void backwardCompatibleWithStoredFormat() throws Exception {
        var json = """
                {
                  "defaultInterval": 1800,
                  "deviceRules": [
                    {
                      "deviceIds": ["EVB-P4562137", "ALF-9571445"],
                      "interval": 600
                    }
                  ],
                  "modelRules": [
                    {
                      "vendor": "Alfen BV",
                      "modelRegex": "NG920-5250[6-9]",
                      "interval": 60
                    }
                  ]
                }
                """;

        var rules = JsonConfiguration.OBJECT_MAPPER.readValue(json, IntervalRules.class);

        assertEquals(Duration.ofSeconds(1800), rules.defaultInterval());
        assertEquals(600, rules.deviceRules().getFirst().interval().toSeconds());
        assertEquals(60, rules.modelRules().getFirst().interval().toSeconds());
        assertEquals("NG920-5250[6-9]", rules.modelRules().getFirst().modelRegex());

        var written = JsonConfiguration.OBJECT_MAPPER.writeValueAsString(rules);
        assertThat(written).isExactlyLike(json);
    }
}
