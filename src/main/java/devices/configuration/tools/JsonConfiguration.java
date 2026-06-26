package devices.configuration.tools;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.*;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.introspect.AnnotatedClassResolver;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.NamedType;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.Duration;
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
            .addModule(durationModule())
            .build();

    private static SimpleModule durationModule() {
        var module = new SimpleModule();
        module.addSerializer(Duration.class, new StdSerializer<>(Duration.class) {
            @Override
            public void serialize(Duration value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
                gen.writeNumber(value.toSeconds());
            }
        });
        module.addDeserializer(Duration.class, new StdDeserializer<>(Duration.class) {
            @Override
            public Duration deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
                return Duration.ofSeconds(p.getValueAsLong());
            }
        });
        return module;
    }

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
