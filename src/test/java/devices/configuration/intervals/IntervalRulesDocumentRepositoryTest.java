package devices.configuration.intervals;

import devices.configuration.IntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

import static devices.configuration.JsonAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@IntegrationTest
@Transactional
class IntervalRulesDocumentRepositoryTest {

    @Autowired
    IntervalRulesDocumentRepository repository;

    @Autowired
    EntityManager entityManager;

    @BeforeEach
    void setUp() {
        repository.save(new IntervalRules(Duration.ofSeconds(1800), List.of(), List.of()));
        entityManager.flush();
    }

    @Test
    void saveAndGet() {
        var rules = new IntervalRules(
                Duration.ofSeconds(1800),
                List.of(new IntervalRules.DeviceRule(List.of("EVB-P4562137"), Duration.ofSeconds(600))),
                List.of(new IntervalRules.ModelRule("Alfen BV", "NG920-5250[6-9]", Duration.ofSeconds(60)))
        );

        repository.save(rules);
        entityManager.flush();
        entityManager.clear();

        IntervalRules read = repository.get();

        assertEquals(Duration.ofSeconds(1800), read.defaultInterval());
        assertEquals(1, read.deviceRules().size());
        assertEquals("EVB-P4562137", read.deviceRules().getFirst().deviceIds().getFirst());
        assertEquals(Duration.ofSeconds(600), read.deviceRules().getFirst().interval());
        assertEquals(1, read.modelRules().size());
        assertEquals("Alfen BV", read.modelRules().getFirst().vendor());
        assertEquals(Duration.ofSeconds(60), read.modelRules().getFirst().interval());
    }

    @Test
    void defaultWhenEmpty() {
        IntervalRules read = repository.get();

        assertEquals(Duration.ofSeconds(1800), read.defaultInterval());
        assertEquals(List.of(), read.deviceRules());
        assertEquals(List.of(), read.modelRules());
    }

    @Test
    void updateExisting() {
        var first = new IntervalRules(Duration.ofSeconds(1800), List.of(), List.of());
        var second = new IntervalRules(Duration.ofSeconds(3600), List.of(), List.of());

        repository.save(first);
        entityManager.flush();
        repository.save(second);
        entityManager.flush();
        entityManager.clear();

        IntervalRules read = repository.get();
        assertEquals(Duration.ofSeconds(3600), read.defaultInterval());
    }

    @Test
    void jsonRoundtrip() {
        var rules = new IntervalRules(
                Duration.ofSeconds(2700),
                List.of(
                        new IntervalRules.DeviceRule(List.of("t53_8264_019", "EVB-P15079256"), Duration.ofSeconds(2700))
                ),
                List.of(
                        new IntervalRules.ModelRule("ChargeStorm AB", "Chargestorm Connected", Duration.ofSeconds(120))
                )
        );

        repository.save(rules);
        entityManager.flush();
        entityManager.clear();

        IntervalRules read = repository.get();
        assertThat(read).isExactlyLike(rules);
    }
}
