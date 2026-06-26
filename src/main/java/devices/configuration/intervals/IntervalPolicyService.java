package devices.configuration.intervals;

import devices.configuration.communication.BootNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@Transactional
@RequiredArgsConstructor
public class IntervalPolicyService {

    private final IntervalRulesRepository repository;

    public Duration calculateFor(BootNotification boot) {
        return repository.get().calculateFor(boot);
    }

    public IntervalRules currentRules() {
        return repository.get();
    }

    public void updateRules(IntervalRules rules) {
        repository.save(rules);
    }
}
