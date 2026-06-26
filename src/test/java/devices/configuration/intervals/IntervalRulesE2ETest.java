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
class IntervalRulesE2ETest {

    @Autowired
    IntervalRulesController controller;

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
    void getDefaultRules() {
        IntervalRules rules = controller.get();

        assertEquals(Duration.ofSeconds(1800), rules.defaultInterval());
        assertEquals(List.of(), rules.deviceRules());
        assertEquals(List.of(), rules.modelRules());
    }

    @Test
    void putAndGetRules() {
        var body = new IntervalRules(
                Duration.ofSeconds(1200),
                List.of(new IntervalRules.DeviceRule(List.of("EVB-P4562137"), Duration.ofSeconds(600))),
                List.of(new IntervalRules.ModelRule("Alfen BV", "NG920-5250[6-9]", Duration.ofSeconds(60)))
        );

        IntervalRules updated = controller.update(body);

        assertEquals(Duration.ofSeconds(1200), updated.defaultInterval());

        IntervalRules read = controller.get();
        assertThat(read).isExactlyLike(body);
    }

    @Test
    void persistedThroughDatabase() {
        var body = new IntervalRules(Duration.ofSeconds(600), List.of(), List.of());

        controller.update(body);
        entityManager.flush();
        entityManager.clear();

        IntervalRules saved = repository.get();
        assertEquals(Duration.ofSeconds(600), saved.defaultInterval());
    }
}
