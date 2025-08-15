package JavaFullstack.AutoSplit.service;

import JavaFullstack.AutoSplit.model.*;
import JavaFullstack.AutoSplit.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;

@Service @RequiredArgsConstructor
public class AlertService {
    private final AlertRepository alertRepo;
    private final RuleEngineService rules;

    @Transactional
    public void thresholdAlerts(User user, String category, double utilizationPct){
        if (utilizationPct >= rules.threshold100())
            save(user, category, AlertType.THRESHOLD_100, "CRITICAL", "You’ve exhausted this month’s budget for " + category, utilizationPct);
        else if (utilizationPct >= rules.threshold80())
            save(user, category, AlertType.THRESHOLD_80, "WARN","You’ve crossed 80% budget for " + category, utilizationPct);
        else if (utilizationPct >= rules.threshold60())
            save(user, category, AlertType.THRESHOLD_60, "INFO","You’ve crossed 60% budget for " + category, utilizationPct);
    }

    @Transactional
    public void burnRateAlert(User user, String category, BigDecimal avgPerDay, BigDecimal budget, int daysLeft, int monthDays, BigDecimal spentSoFar){
        if (budget==null || budget.signum()==0) return;
        BigDecimal projected = spentSoFar.add(avgPerDay.multiply(BigDecimal.valueOf(daysLeft)));
        int comp = projected.compareTo(budget);
        if (comp > 0){
            save(user, category, AlertType.BURN_RATE, "WARN",
                    "At current pace, you’ll exceed budget before month end in " + category, null);
        }
    }

    @Transactional
    public void spikeAlert(User user, String category, BigDecimal dayAmount, BigDecimal budget){
        double p = (budget==null || budget.signum()==0) ? 0.0 : dayAmount.divide(budget,4,java.math.RoundingMode.HALF_UP).doubleValue();
        if (p >= rules.spikePercent()){
            save(user, category, AlertType.SPIKE, "WARN",
                    "Unusual spike: today’s spend > "+(int)(rules.spikePercent()*100)+"% of "+category+" budget.", p);
        }
    }

    @Transactional
    public void underuseAlert(User user, String category){
        save(user, category, AlertType.UNDERUSE, "INFO",
                "Underused budget in " + category + " — consider reallocating.", null);
    }

    @Transactional
    public void recurringAlert(User user, String merchant){
        save(user, null, AlertType.RECURRING, "INFO",
                "Recurring expense detected: " + merchant, null);
    }

    @Transactional
    public void goalAlert(User user, String msg){
        save(user, null, AlertType.GOAL, "INFO", msg, null);
    }

    @Transactional
    public void achievement(User user, String category, String msg){
        save(user, category, AlertType.ACHIEVEMENT, "INFO", msg, null);
    }

    private void save(User user, String category, AlertType type, String severity, String message, Double metric){
        Alert a = Alert.builder().user(user).category(category).type(type).severity(severity)
                .message(message).metric(metric).createdAt(java.time.LocalDateTime.now()).build();
        alertRepo.save(a);
    }
}
