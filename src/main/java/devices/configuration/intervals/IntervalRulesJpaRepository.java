package devices.configuration.intervals;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
class IntervalRulesJpaRepository implements IntervalRulesRepository {

    private static final String DEFAULT_ID = "default";

    private final RulesSpringRepository springRepository;

    @Override
    public IntervalRules get() {
        return springRepository.findById(DEFAULT_ID)
                .map(IntervalRulesEntity::getRules)
                .orElse(IntervalRules.withDefaults());
    }

    @Override
    @Transactional
    public void save(IntervalRules rules) {
        springRepository.save(
                springRepository.findById(DEFAULT_ID)
                        .orElseGet(() -> new IntervalRulesEntity(DEFAULT_ID))
                        .setRules(rules)
        );
    }

    @Repository
    interface RulesSpringRepository extends JpaRepository<IntervalRulesEntity, String> {}

    @Entity
    @Table(name = "interval_rules")
    @NoArgsConstructor
    static class IntervalRulesEntity {
        @Id
        private String id;

        @Version
        private long version;

        @Type(JsonBinaryType.class)
        private IntervalRules rules;

        IntervalRulesEntity(String id) {
            this.id = id;
        }

        IntervalRulesEntity setRules(IntervalRules rules) {
            this.rules = rules;
            return this;
        }

        IntervalRules getRules() {
            return rules;
        }
    }
}
