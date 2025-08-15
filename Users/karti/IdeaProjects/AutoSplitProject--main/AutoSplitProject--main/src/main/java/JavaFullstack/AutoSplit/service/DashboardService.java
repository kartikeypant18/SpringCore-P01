package JavaFullstack.AutoSplit.service;

import JavaFullstack.AutoSplit.model.User;
import JavaFullstack.AutoSplit.repository.SplitTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private SplitTransactionRepository splitTransactionRepository;

    public Map<String, Object> getMonthDashboard(User user, LocalDateTime start, LocalDateTime end) {
        var txs = splitTransactionRepository.findByUserAndCreatedAtBetween(user, start, end);

        Map<String, Double> sums = txs.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategoryName() == null ? "Uncategorized" : t.getCategoryName(),
                        Collectors.summingDouble(t -> t.getAmount() == null ? 0.0 : t.getAmount())
                ));

        double total = sums.values().stream().mapToDouble(Double::doubleValue).sum();

        List<Map<String, Object>> splits = new ArrayList<>();
        for (var e : sums.entrySet()) {
            Map<String, Object> categoryData = new HashMap<>();
            categoryData.put("category", e.getKey());
            categoryData.put("amount", Math.round(e.getValue() * 100.0) / 100.0);
            categoryData.put("percentage", total == 0 ? 0.0 : Math.round((e.getValue() / total) * 10000.0) / 100.0);
            splits.add(categoryData);
        }

        splits.sort((a, b) -> Double.compare((double) b.get("amount"), (double) a.get("amount")));

        Map<String, Object> response = new HashMap<>();
        response.put("month", String.format("%04d-%02d", start.getYear(), start.getMonthValue()));
        response.put("splits", splits);
        response.put("total", Math.round(total * 100.0) / 100.0);

        return response;
    }

    public List<Map<String, Object>> getHistory(User user, int months) {
        List<Map<String, Object>> history = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 0; i < months; i++) {
            LocalDate target = now.minusMonths(i);
            LocalDateTime start = target.withDayOfMonth(1).atStartOfDay();
            LocalDateTime end = target.withDayOfMonth(target.lengthOfMonth()).atTime(23, 59, 59);
            history.add(getMonthDashboard(user, start, end));
        }
        Collections.reverse(history);
        return history;
    }
}
