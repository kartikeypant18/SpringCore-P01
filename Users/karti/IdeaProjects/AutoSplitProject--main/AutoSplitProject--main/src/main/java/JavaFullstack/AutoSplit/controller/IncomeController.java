package JavaFullstack.AutoSplit.controller;

import JavaFullstack.AutoSplit.model.Income;
import JavaFullstack.AutoSplit.model.SplitTransaction;
import JavaFullstack.AutoSplit.model.User;
import JavaFullstack.AutoSplit.service.IncomeService;
import JavaFullstack.AutoSplit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/incomes")
public class IncomeController {

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private UserService userService;

    /**
     * Add a new income and auto-split into transactions based on saved IncomeSplits
     */
    @PostMapping("/{userId}")
    public ResponseEntity<?> addIncome(@PathVariable Long userId,
                                       @RequestBody Map<String, Object> body,
                                       Principal principal) {

        // Ensure the logged-in user is adding income only for themselves
        Optional<User> currentUser = userService.findByEmail(principal.getName());
        if (currentUser.isEmpty() || !currentUser.get().getId().equals(userId)) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "You are not allowed to add income for another user"));
        }

        // Validate and extract amount
        Double amount;
        String source = null;
        try {
            if (!body.containsKey("amount")) {
                return ResponseEntity.badRequest().body(Map.of("message", "'amount' is required"));
            }
            amount = Double.parseDouble(body.get("amount").toString());
            if (amount <= 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "Amount must be greater than 0"));
            }
            if (body.containsKey("source")) {
                source = body.get("source").toString().trim();
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "'amount' must be a valid number"));
        }

        // Save income and auto-create split transactions
        Income saved = incomeService.addIncomeAndCreateSplits(currentUser.get(), amount, source);

        return ResponseEntity.ok(Map.of(
                "incomeId", saved.getId(),
                "amount", saved.getAmount(),
                "source", saved.getSource(),
                "message", "Income added successfully and split transactions created"
        ));
    }

    /**
     * Get split transaction history for the given user
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<?> history(@PathVariable Long userId, Principal principal) {
        Optional<User> currentUser = userService.findByEmail(principal.getName());
        if (currentUser.isEmpty() || !currentUser.get().getId().equals(userId)) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "You are not allowed to view another user's transaction history"));
        }

        List<SplitTransaction> txs = incomeService.getSplitHistory(currentUser.get());
        return ResponseEntity.ok(txs);
    }
}
