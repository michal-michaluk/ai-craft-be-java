package devices.configuration.communication;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public record DeviceStatuses(String deviceId, List<String> statuses) {

    public <T> List<T> map(Function<String, T> mapper) {
        return statuses().stream()
                .map(mapper)
                .collect(Collectors.toList());
    }
}
