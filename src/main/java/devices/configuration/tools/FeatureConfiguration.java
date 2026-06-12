package devices.configuration.tools;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class FeatureConfiguration {

    private final ConfigurationRepository repository;

    public Configuration get(String name) {
        return repository.findByName(name)
                .map(entity -> new Configuration(entity.name, entity.configuration))
                .orElseGet(() -> new Configuration(name, null));
    }

    public Configuration save(String name, Object configuration) {
        var entity = repository.findByName(name)
                .orElseGet(() -> new ConfigurationEntity(name));
        JsonNode json = JsonConfiguration.OBJECT_MAPPER.valueToTree(configuration);
        entity.configuration = json;
        repository.save(entity);
        return new Configuration(name, json);
    }

    public record Configuration(String name, JsonNode configuration) {
        public <T> Optional<T> as(Class<T> type) {
            if (configuration == null) {
                return Optional.empty();
            } else try {
                return Optional.of(JsonConfiguration.OBJECT_MAPPER.treeToValue(configuration, type));
            } catch (JacksonException e) {
                throw new RuntimeException(e);
            }
        }

        public JsonNode raw() {
            return configuration;
        }
    }

    @Repository
    interface ConfigurationRepository extends CrudRepository<ConfigurationEntity, String> {
        Optional<ConfigurationEntity> findByName(String name);
    }

    @Entity
    @Table(name = "features_configuration")
    static class ConfigurationEntity {
        @Id
        private String name;
        @JdbcTypeCode(SqlTypes.JSON)
        private JsonNode configuration;

        public ConfigurationEntity(String name) {
            this.name = name;
        }

        public ConfigurationEntity() {
        }
    }
}
