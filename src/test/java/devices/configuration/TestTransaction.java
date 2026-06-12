package devices.configuration;

import java.util.function.Supplier;

import static org.springframework.test.context.transaction.TestTransaction.*;

public class TestTransaction {

    public static void transactional(Runnable body) {
        if (!isActive()) {
            start();
        }
        body.run();
        flagForCommit();
        end();
    }

    public static <T> T transactional(Supplier<T> body) {
        if (!isActive()) {
            start();
        }
        T result = body.get();
        flagForCommit();
        end();
        return result;
    }
}
