package devices.configuration.intervals;

interface IntervalRulesRepository {

    IntervalRules get();

    void save(IntervalRules rules);
}
