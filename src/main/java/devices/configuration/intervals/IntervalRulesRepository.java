package devices.configuration.intervals;

public interface IntervalRulesRepository {
    IntervalRules get();
    void save(IntervalRules rules);
}
