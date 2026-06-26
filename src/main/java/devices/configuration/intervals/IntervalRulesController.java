package devices.configuration.intervals;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class IntervalRulesController {

    private final IntervalPolicyService service;

    @GetMapping("/intervals")
    IntervalRules get() {
        return service.currentRules();
    }

    @PutMapping("/intervals")
    IntervalRules update(@RequestBody IntervalRules rules) {
        service.updateRules(rules);
        return service.currentRules();
    }
}
