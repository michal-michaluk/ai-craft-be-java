package devices.configuration.tools;

import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class EventTypes {

    private static Map<Class<?>, Type> mapping;

    public static Type of(Object event) {
        return of(event.getClass());
    }

    public static Type of(Class<?> type) {
        return mapping.get(type);
    }

    public record Type(String type, String version) {
        public static Type of(String typeName) {
            String[] parts = typeName.split("_v");
            if (parts.length != 2 || parts[1] == null || parts[1].isBlank()) {
                throw new IllegalArgumentException(
                        "Version required in JsonSubTypes name on top of DomainEvent interface, like StationProtocolChanged_v1, '_v' part is important, thrown for type name: " + typeName
                );
            }
            return new Type(parts[0], parts[1]);
        }
    }

    public static void init(Map<Class<?>, Type> mapping) {
        EventTypes.mapping = mapping;
    }
}
