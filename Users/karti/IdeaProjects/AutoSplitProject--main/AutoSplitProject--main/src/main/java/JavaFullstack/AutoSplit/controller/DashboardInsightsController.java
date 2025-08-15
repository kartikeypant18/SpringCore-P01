package JavaFullstack.AutoSplit.controller;

import JavaFullstack.AutoSplit.model.*;
import JavaFullstack.AutoSplit.repository.*;
import JavaFullstack.AutoSplit.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardInsightsController {
    private final AnalyticsService analytics;
    private final BudgetRepository budgetRepo;
    private final ExpenseRepository expenseRepo;
    private final AlertService alertService;
    private final GoalRepository goalRepo;
    private final UserService userService;
    private final IncomeRepository incomeRepo;
    @GetMapping("/insights")
    public ResponseEntity<?> insights(@RequestHeader("Authorization") String auth,
                                      @RequestParam(required=false) Integer year,
                                      @RequestParam(required=false) Integer month){
        User user = userService.getCurrentUser(auth).orElseThrow();
        YearMonth ym = (year!=null && month!=null)? YearMonth.of(year, month) : YearMonth.now();

        // 1) Pull month income from your existing services (you have Income & IncomeSplit already)
        BigDecimal monthIncome = getMonthIncomeForUser(user, ym);

        // 2) Utilization
        var utilization = analytics.computeUtilization(user.getId(), ym, monthIncome);

        // 3) Threshold alerts (60/80/100)
        utilization.values().forEach(u -> alertService.thresholdAlerts(user, u.category(), u.utilizationPct()));

        // 4) Burn rate
        utilization.values().forEach(u -> {
            var br = analytics.computeBurnRate(user.getId(), ym, u.category());
            if (br.containsKey(u.category())){
                var b = br.get(u.category());
                alertService.burnRateAlert(user, u.category(), b.avgDaily(), u.budget(), b.daysLeft(), b.monthDays(), u.spent());
            }
        });

        // 5) Underuse (call this near end-of-month or on demand)
        utilization.values().forEach(u -> {
            boolean isMonthEnd = ym.atEndOfMonth().minusDays(3).isBefore(java.time.LocalDate.now());
            if (isMonthEnd && analytics.isUnderuse(u.spent(), u.budget()))
                alertService.underuseAlert(user, u.category());
        });

        // 6) Recurring expense detection
        var recurring = analytics.recurringMerchants(user.getId(), ym, 2);
        recurring.forEach(m -> alertService.recurringAlert(user, m));

        // 7) Trends (compare with last 3–6 months)
        var trends = analytics.monthlyTrends(user.getId(), ym, 3);

        // 8) Goals & suggestions
        var goals = goalRepo.findByUserId(user.getId());
        List<String> goalSuggestions = new ArrayList<>();
        for (Goal g : goals) {
            goalSuggestions.add("Goal \""+g.getName()+"\": " + (new java.text.DecimalFormat("#,##0.00")).format(g.getSavedAmount())
                    + " / " + (new java.text.DecimalFormat("#,##0.00")).format(g.getTargetAmount()));
        }

        // 9) Balance Shift suggestion (overspent vs underused)
        List<Map<String,Object>> balanceShifts = new ArrayList<>();
        var overs = utilization.values().stream().filter(u->u.utilizationPct()>1.0).toList();
        var unders = utilization.values().stream().filter(u->u.utilizationPct()<0.5).toList();
        for (var o : overs){
            for (var un : unders){
                if (un.budget().compareTo(un.spent())>0){
                    var possible = un.budget().subtract(un.spent());
                    Map<String,Object> rec = new HashMap<>();
                    rec.put("from", un.category());
                    rec.put("to", o.category());
                    rec.put("amount", possible.min(o.spent().subtract(o.budget())));
                    balanceShifts.add(rec);
                }
            }
        }

        // Response payload (Feature 14)
        Map<String,Object> payload = new HashMap<>();
        payload.put("year", ym.getYear());
        payload.put("month", ym.getMonthValue());
        payload.put("income", monthIncome);
        payload.put("utilization", utilization);   // category -> spent/budget/utilizationPct
        payload.put("trends", trends);             // category -> current/avgPrev/variancePct
        payload.put("recurringMerchants", recurring);
        payload.put("balanceShiftSuggestions", balanceShifts);
        payload.put("goalSummaries", goalSuggestions);

        return ResponseEntity.ok(payload);
    }
    private BigDecimal getMonthIncomeForUser(User user, YearMonth ym) {
        double total = getMonthlyIncome(user.getId(), ym.getYear(), ym.getMonthValue());
        return BigDecimal.valueOf(total);
    }

    public double getMonthlyIncome(Long userId, int year, int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);
        return incomeRepo.findByUserIdAndCreatedAtBetween(userId, start, end)
                .stream()
                .mapToDouble(Income::getAmount)
                .sum();
    }

}
