package devices.configuration.intervals;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Repository
@AllArgsConstructor
class IntervalRulesDocumentRepository implements IntervalRulesRepository {

    private final DocumentRepository documents;

    @Override
    public IntervalRules get() {
        return documents.findById("default")
                .map(e -> e.rules)
                .orElse(new IntervalRules(Duration.ofSeconds(1800), List.of(), List.of()));
    }

    @Override
    public void save(IntervalRules rules) {
        documents.save(documents.findById("default")
                .orElseGet(() -> new IntervalRulesEntity(rules))
                .setRules(rules));
    }

    @Repository
    interface DocumentRepository extends CrudRepository<IntervalRulesEntity, String> {
    }

    @Entity
    @Table(name = "interval_rules")
    @NoArgsConstructor
    static class IntervalRulesEntity {
        @Id
        private String id = "default";

        @JdbcTypeCode(SqlTypes.JSON)
        private IntervalRules rules;

        IntervalRulesEntity(IntervalRules rules) {
            this.rules = rules;
        }

        IntervalRulesEntity setRules(IntervalRules rules) {
            this.rules = rules;
            return this;
        }
    }
}
