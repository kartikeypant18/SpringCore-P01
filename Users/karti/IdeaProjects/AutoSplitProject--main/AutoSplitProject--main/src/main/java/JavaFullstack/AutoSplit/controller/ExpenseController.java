package JavaFullstack.AutoSplit.controller;

import JavaFullstack.AutoSplit.model.Expense;
import JavaFullstack.AutoSplit.model.User;
import JavaFullstack.AutoSplit.service.ExpenseService;
import JavaFullstack.AutoSplit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Expense> log(@RequestHeader("Authorization") String auth,
                                       @RequestBody Req req){
        User user = userService.getCurrentUser(auth).orElseThrow();
        Expense e = expenseService.logExpense(user, req.category().trim().toLowerCase(), req.amount(),
                req.date()==null? LocalDate.now() : req.date(), req.merchant(), req.note());
        return ResponseEntity.ok(e);
    }

    public record Req(String category, BigDecimal amount, LocalDate date, String merchant, String note) {}
}
