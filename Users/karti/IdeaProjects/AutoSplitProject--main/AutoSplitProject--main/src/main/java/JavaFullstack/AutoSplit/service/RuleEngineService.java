package JavaFullstack.AutoSplit.service;

import JavaFullstack.AutoSplit.repository.RuleRepository;
import JavaFullstack.AutoSplit.model.Rule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service @RequiredArgsConstructor
public class RuleEngineService {
    private final RuleRepository ruleRepo;
    private final Map<String,String> cache = new ConcurrentHashMap<>();

    public double getDouble(String key, double defaultVal) {
        return Double.parseDouble(cache.computeIfAbsent(key, k ->
                ruleRepo.findByKeyName(k).map(Rule::getValue).orElse(String.valueOf(defaultVal))));
    }
    public String getString(String key, String defaultVal) {
        return cache.computeIfAbsent(key, k ->
                ruleRepo.findByKeyName(k).map(Rule::getValue).orElse(defaultVal));
    }
    // Defaults
    public double threshold60() { return getDouble("threshold.60", 0.60); }
    public double threshold80() { return getDouble("threshold.80", 0.80); }
    public double threshold100(){ return getDouble("threshold.100",1.00); }
    public double spikePercent(){ return getDouble("spike.percent", 0.30); }      // 30%
    public double underusePercent(){ return getDouble("underuse.percent", 0.20);} // 20%
}
