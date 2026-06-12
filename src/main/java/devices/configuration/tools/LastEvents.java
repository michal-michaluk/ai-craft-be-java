package devices.configuration.tools;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class LastEvents {
    Map<? extends Class<?>, ?> last;

    public static LastEvents fromHistoryOf(List<?> events) {
        return new LastEvents(events.stream()
                .collect(Collectors.toMap(
                        Object::getClass,
                        Function.identity(),
                        (last, previous) -> last
                )));
    }

    public <R, T> R getOrNull(Class<T> type, Function<T, R> fun) {
        return Optional.ofNullable(last.get(type))
                .map(type::cast)
                .map(fun)
                .orElse(null);
    }

    public <R, T> R getOrDefault(Class<T> type, Function<T, R> fun, R defaultValue) {
        return Optional.ofNullable(last.get(type))
                .map(type::cast)
                .map(fun)
                .orElse(defaultValue);
    }
}
