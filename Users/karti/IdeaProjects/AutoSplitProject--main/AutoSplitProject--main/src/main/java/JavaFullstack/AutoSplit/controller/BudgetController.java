package JavaFullstack.AutoSplit.controller;

import JavaFullstack.AutoSplit.model.*;
import JavaFullstack.AutoSplit.service.BudgetService;
import JavaFullstack.AutoSplit.service.UserService;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetService budgetService;
    private final UserService userService;

    @PostMapping("/setup")
    public ResponseEntity<?> setup(@RequestHeader("Authorization") String auth,
                                   @RequestBody List<BudgetReq> budgets){
        User user = userService.getCurrentUser(auth).orElseThrow();
        YearMonth ym = YearMonth.now();
        budgets.forEach(b -> budgetService.upsertBudget(user, b.category(), ym, b.type(), b.percent(), b.fixed()));
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestHeader("Authorization") String auth,
                                  @RequestParam(required=false) Integer year,
                                  @RequestParam(required=false) Integer month){
        User user = userService.getCurrentUser(auth).orElseThrow();
        var ym = (year!=null && month!=null)? YearMonth.of(year,month) : YearMonth.now();
        return ResponseEntity.ok(budgetService.getBudgets(user, ym));
    }

    public record BudgetReq(String category, BudgetType type, BigDecimal percent, BigDecimal fixed) {}
}
