package devices.configuration.tools;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.*;
import tools.jackson.databind.introspect.AnnotatedClassResolver;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.NamedType;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

@Configuration
public class JsonConfiguration {

    public static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .changeDefaultVisibility(v -> v
                    .withVisibility(PropertyAccessor.CREATOR, ANY)
                    .withVisibility(PropertyAccessor.FIELD, ANY))
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false)
            .configure(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN, true)
            .build();

    @Bean
    ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }

    @PostConstruct
    private void initEventTypes() {
        DeserializationConfig config = OBJECT_MAPPER.deserializationConfig();
        AnnotationIntrospector ai = config.getAnnotationIntrospector();
        Map<Class<?>, EventTypes.Type> subtypes = Stream.of(
                        devices.configuration.device.DomainEvent.class
                )
                .map(type -> AnnotatedClassResolver.resolve(config, OBJECT_MAPPER.constructType(type), config))
                .flatMap(type -> ai.findSubtypes(config, type).stream())
                .collect(Collectors.toMap(
                        NamedType::getType,
                        type -> EventTypes.Type.of(type.getName())
                ));
        EventTypes.init(subtypes);
    }
}
