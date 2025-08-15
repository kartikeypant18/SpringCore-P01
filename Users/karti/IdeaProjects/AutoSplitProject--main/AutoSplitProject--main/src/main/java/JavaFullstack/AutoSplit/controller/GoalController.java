package JavaFullstack.AutoSplit.controller;

import JavaFullstack.AutoSplit.model.Goal;
import JavaFullstack.AutoSplit.model.User;
import JavaFullstack.AutoSplit.service.GoalService;
import JavaFullstack.AutoSplit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Goal> createGoal(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody GoalRequest request
    ) {
        User user = userService.getCurrentUser(authHeader)
                .orElseThrow(() -> new RuntimeException("Invalid or missing token"));

        Goal g = goalService.createOrUpdate(
                user,
                request.getName(),
                request.getTargetAmount(),
                request.getSavedAmount(),
                request.getTargetDate()
        );

        return ResponseEntity.ok(g);
    }

    @GetMapping
    public ResponseEntity<List<Goal>> getGoals(@RequestHeader("Authorization") String authHeader) {
        User user = userService.getCurrentUser(authHeader)
                .orElseThrow(() -> new RuntimeException("Invalid or missing token"));

        return ResponseEntity.ok(goalService.getGoalsByUser(user.getId()));
    }
    // DTO for request body
    public static class GoalRequest {
        private String name;
        private BigDecimal targetAmount;
        private BigDecimal savedAmount;
        private LocalDate targetDate;

        public String getName() { return name; }
        public BigDecimal getTargetAmount() { return targetAmount; }
        public BigDecimal getSavedAmount() { return savedAmount; }
        public LocalDate getTargetDate() { return targetDate; }
    }
}
