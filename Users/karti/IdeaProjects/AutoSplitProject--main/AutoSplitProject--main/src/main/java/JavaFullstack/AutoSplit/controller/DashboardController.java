package JavaFullstack.AutoSplit.controller;

import JavaFullstack.AutoSplit.model.SplitTransaction;
import JavaFullstack.AutoSplit.model.User;
import JavaFullstack.AutoSplit.repository.IncomeRepository;
import JavaFullstack.AutoSplit.repository.SplitTransactionRepository;
import JavaFullstack.AutoSplit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private SplitTransactionRepository splitTransactionRepository;

    // ========================= SUMMARY =========================
    @GetMapping("/summary/{userId}")
    public ResponseEntity<?> getSummary(@PathVariable Long userId) {
        User user = getUser(userId);
System.out.println("Call ho gyi api iske sath:"+user);
        double totalIncome = incomeRepository.findByUser(user).stream()
                .mapToDouble(i -> Optional.ofNullable(i.getAmount()).orElse(0.0))
                .sum();

        List<SplitTransaction> transactions = splitTransactionRepository.findByUser(user);

        Map<String, Double> categoryTotals = transactions.stream()
                .collect(Collectors.groupingBy(
                        tx -> Optional.ofNullable(tx.getCategoryName()).orElse("Uncategorized"),
                        Collectors.summingDouble(tx -> Optional.ofNullable(tx.getAmount()).orElse(0.0))
                ));

        return ResponseEntity.ok(Map.of(
                "totalIncome", totalIncome,
                "categoryTotals", categoryTotals,
                "transactionsCount", transactions.size()
        ));
    }

    // ========================= LAST N MONTHS CATEGORY TOTALS =========================
    @GetMapping("/monthly/{userId}")
    public ResponseEntity<?> getMonthly(@PathVariable Long userId,
                                        @RequestParam(defaultValue = "3") int months) {
        User user = getUser(userId);
System.out.println("Sun call hogyi Monthly walli :"+user);
        List<SplitTransaction> transactions = splitTransactionRepository.findByUser(user);

        Map<YearMonth, Map<String, Double>> monthlyData = new TreeMap<>();
        transactions.forEach(tx -> {
            YearMonth ym = YearMonth.from(tx.getCreatedAt());
            monthlyData.computeIfAbsent(ym, k -> new HashMap<>());
            monthlyData.get(ym).merge(
                    Optional.ofNullable(tx.getCategoryName()).orElse("Uncategorized"),
                    Optional.ofNullable(tx.getAmount()).orElse(0.0),
                    Double::sum
            );
        });

        List<YearMonth> lastMonths = monthlyData.keySet().stream()
                .sorted()
                .skip(Math.max(0, monthlyData.size() - months))
                .toList();

        return ResponseEntity.ok(Map.of(
                "months", lastMonths.stream().map(YearMonth::toString).toList(),
                "series", lastMonths.stream().collect(Collectors.toMap(
                        ym -> ym.toString(),
                        monthlyData::get,
                        (a, b) -> b,
                        LinkedHashMap::new
                ))
        ));
    }

    // ========================= CURRENT MONTH PIE CHART =========================
    @GetMapping("/current-month/{userId}")
    public ResponseEntity<?> getCurrentMonth(@PathVariable Long userId) {
        User user = getUser(userId);
System.out.println("sun current month walli call ho gy:"+user);
        LocalDate now = LocalDate.now();
        LocalDateTime start = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59);

        List<SplitTransaction> transactions = splitTransactionRepository.findByUserAndCreatedAtBetween(user, start, end);

        Map<String, Double> byCategory = transactions.stream()
                .collect(Collectors.groupingBy(
                        tx -> Optional.ofNullable(tx.getCategoryName()).orElse("Uncategorized"),
                        Collectors.summingDouble(tx -> Optional.ofNullable(tx.getAmount()).orElse(0.0))
                ));

        double total = byCategory.values().stream().mapToDouble(Double::doubleValue).sum();
        List<Map<String, Object>> breakdown = byCategory.entrySet().stream()
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("category", e.getKey());
                    map.put("amount", e.getValue());
                    map.put("percentage", total == 0 ? 0.0 : (e.getValue() / total) * 100);
                    return map;
                })
                .collect(Collectors.toList());


        return ResponseEntity.ok(Map.of(
                "month", now.getMonth().toString(),
                "year", now.getYear(),
                "total", total,
                "breakdown", breakdown
        ));
    }

    // ========================= HISTORY LINE CHART =========================
    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getHistory(@PathVariable Long userId,
                                        @RequestParam(defaultValue = "6") int months) {
        User user = getUser(userId);
        List<Map<String, Object>> history = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = months - 1; i >= 0; i--) {
            LocalDate target = now.minusMonths(i);
            LocalDateTime start = target.withDayOfMonth(1).atStartOfDay();
            LocalDateTime end = target.withDayOfMonth(target.lengthOfMonth()).atTime(23, 59, 59);

            List<SplitTransaction> txs = splitTransactionRepository.findByUserAndCreatedAtBetween(user, start, end);

            double total = txs.stream()
                    .mapToDouble(tx -> Optional.ofNullable(tx.getAmount()).orElse(0.0))
                    .sum();

            history.add(Map.of(
                    "month", target.getMonth().toString(),
                    "year", target.getYear(),
                    "total", total
            ));
        }

        return ResponseEntity.ok(history);
    }

    // ========================= UTILITY =========================
    private User getUser(Long userId) {
        return userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
