package devices.configuration.tools;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.format.FormatMapper;

import tools.jackson.core.JacksonException;

public class Jackson3JsonFormatMapper implements FormatMapper {

    private final tools.jackson.databind.ObjectMapper objectMapper = JsonConfiguration.OBJECT_MAPPER;

    @Override
    public <T> T fromString(CharSequence charSequence, JavaType<T> javaType, WrapperOptions wrapperOptions) {
        try {
            return objectMapper.readValue(
                    charSequence instanceof String s ? s : charSequence.toString(),
                    objectMapper.getTypeFactory().constructType(javaType.getJavaType())
            );
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to deserialize JSON", e);
        }
    }

    @Override
    public <T> String toString(T value, JavaType<T> javaType, WrapperOptions wrapperOptions) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }
    }
}
