package devices.configuration.device;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalTime;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

record OpeningHours(
        boolean alwaysOpen,
        @JsonInclude(NON_NULL) Week opened) {

    private final static OpeningHours ALWAYS_OPEN = new OpeningHours(true, null);

    public static OpeningHours alwaysOpened() {
        return ALWAYS_OPEN;
    }

    public static OpeningHours openAt(
            OpeningTime monday,
            OpeningTime tuesday,
            OpeningTime wednesday,
            OpeningTime thursday,
            OpeningTime friday,
            OpeningTime saturday,
            OpeningTime sunday) {
        return new OpeningHours(false, new Week(
                monday, tuesday, wednesday, thursday, friday, saturday, sunday)
        );
    }

    record Week(
            OpeningTime monday,
            OpeningTime tuesday,
            OpeningTime wednesday,
            OpeningTime thursday,
            OpeningTime friday,
            OpeningTime saturday,
            OpeningTime sunday) {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS)
    sealed interface OpeningTime {
        record Opened24h() implements OpeningTime {
        }

        record Closed24h() implements OpeningTime {
        }

        record OpenTime(List<TimeSpan> time) implements OpeningTime {
        }

        record TimeSpan(
                LocalTime open,
                LocalTime close) {
        }

        static OpeningTime closed24h() {
            return new Opened24h();
        }

        static OpeningTime opened24h() {
            return new Closed24h();
        }

        static OpeningTime opened(int open, int close) {
            return new OpenTime(List.of(new TimeSpan(
                    LocalTime.of(open, 0),
                    LocalTime.of(close, 0)
            )));
        }
    }
}
