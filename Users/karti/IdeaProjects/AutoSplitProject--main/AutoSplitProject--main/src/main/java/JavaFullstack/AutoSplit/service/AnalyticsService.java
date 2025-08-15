package JavaFullstack.AutoSplit.service;

import JavaFullstack.AutoSplit.model.*;
import JavaFullstack.AutoSplit.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class AnalyticsService {
    private final ExpenseRepository expenseRepo;
    private final BudgetRepository budgetRepo;
    private final RuleEngineService rules;

    public record Utilization(String category, BigDecimal spent, BigDecimal budget, double utilizationPct) {}

    public Map<String, Utilization> computeUtilization(Long userId, YearMonth ym, BigDecimal monthIncome){
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        // budgets
        Map<String, BigDecimal> budgetPerCat = new HashMap<>();
        for (Budget b : budgetRepo.findByUserIdAndYearAndMonth(userId, ym.getYear(), ym.getMonthValue())){
            BigDecimal alloc = switch (b.getType()){
                case PERCENTAGE -> monthIncome.multiply(b.getPercentOfIncome()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                case FIXED -> b.getFixedAmount();
            };
            budgetPerCat.put(b.getCategory(), alloc);
        }
        // spent by category
        Map<String, BigDecimal> spentPerCat = new HashMap<>();
        for (Object[] row : expenseRepo.sumByCategory(userId, start, end)){
            String cat = (String) row[0];
            BigDecimal sum = (BigDecimal) row[1];
            spentPerCat.put(cat, sum);
        }

        Map<String, Utilization> result = new HashMap<>();
        for (var entry : budgetPerCat.entrySet()){
            String cat = entry.getKey();
            BigDecimal budget = entry.getValue();
            BigDecimal spent = spentPerCat.getOrDefault(cat, BigDecimal.ZERO);
            double pct = budget.signum()==0 ? 0.0 : spent.divide(budget, 4, RoundingMode.HALF_UP).doubleValue();
            result.put(cat, new Utilization(cat, spent, budget, pct));
        }
        return result;
    }

    public record BurnRate(String category, BigDecimal avgDaily, Integer daysLeft, Integer monthDays) {}

    public Map<String, BurnRate> computeBurnRate(Long userId, YearMonth ym, String category){
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        List<Object[]> rows = expenseRepo.dailySums(userId, category, start, end);
        if (rows.isEmpty()) return Map.of();
        BigDecimal total = rows.stream().map(r -> (BigDecimal) r[1]).reduce(BigDecimal.ZERO, BigDecimal::add);
        int daysSoFar = (int) rows.size();
        BigDecimal avgPerDay = total.divide(BigDecimal.valueOf(daysSoFar), 2, RoundingMode.HALF_UP);
        int daysInMonth = ym.lengthOfMonth();
        int remaining = daysInMonth - LocalDate.now().getDayOfMonth() + 1; // inclusive today if you prefer
        return Map.of(category, new BurnRate(category, avgPerDay, remaining, daysInMonth));
    }

    public boolean isSpike(Long userId, YearMonth ym, String category, BigDecimal dayAmount, BigDecimal budget){
        if (budget==null || budget.signum()==0) return false;
        double p = dayAmount.divide(budget, 4, RoundingMode.HALF_UP).doubleValue();
        return p >= rules.spikePercent();
    }

    public boolean isUnderuse(BigDecimal spent, BigDecimal budget){
        if (budget==null || budget.signum()==0) return false;
        double p = spent.divide(budget, 4, RoundingMode.HALF_UP).doubleValue();
        return p < rules.underusePercent();
    }

    public List<String> recurringMerchants(Long userId, YearMonth ym, int minCount){
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return expenseRepo.recurringMerchants(userId, start, end, minCount).stream()
                .map(r -> (String) r[0]).filter(Objects::nonNull).collect(Collectors.toList());
    }

    // Trend compare vs last N months
    public record Trend(String category, BigDecimal current, BigDecimal avgPrev, double variancePct) {}
    public Map<String, Trend> monthlyTrends(Long userId, YearMonth current, int backMonths){
        LocalDate cStart = current.atDay(1), cEnd = current.atEndOfMonth();
        Map<String, BigDecimal> currentSums = new HashMap<>();
        for (Object[] r : expenseRepo.sumByCategory(userId, cStart, cEnd)){
            currentSums.put((String)r[0], (BigDecimal)r[1]);
        }
        Map<String, BigDecimal> prevAvg = new HashMap<>();
        for (int i=1;i<=backMonths;i++){
            YearMonth ym = current.minusMonths(i);
            LocalDate s = ym.atDay(1), e = ym.atEndOfMonth();
            for (Object[] r : expenseRepo.sumByCategory(userId, s, e)){
                prevAvg.merge((String)r[0], (BigDecimal)r[1], BigDecimal::add);
            }
        }
        if (backMonths>0) prevAvg.replaceAll((k,v)-> v.divide(BigDecimal.valueOf(backMonths),2, RoundingMode.HALF_UP));
        Map<String, Trend> out = new HashMap<>();
        for (var e : currentSums.entrySet()){
            String cat = e.getKey();
            BigDecimal cur = e.getValue();
            BigDecimal avg = prevAvg.getOrDefault(cat, BigDecimal.ZERO);
            double varPct = (avg.signum()==0) ? 1.0 : cur.divide(avg,4,RoundingMode.HALF_UP).doubleValue() - 1.0;
            out.put(cat, new Trend(cat, cur, avg, varPct));
        }
        return out;
    }
}
