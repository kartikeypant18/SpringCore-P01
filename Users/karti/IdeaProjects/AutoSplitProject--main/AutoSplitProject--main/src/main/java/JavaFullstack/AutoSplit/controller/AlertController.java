package JavaFullstack.AutoSplit.controller;

import JavaFullstack.AutoSplit.repository.AlertRepository;
import JavaFullstack.AutoSplit.repository.UserRepository;
import JavaFullstack.AutoSplit.service.UserService;
import JavaFullstack.AutoSplit.service.AlertService;
import JavaFullstack.AutoSplit.model.Alert;
import JavaFullstack.AutoSplit.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertRepository alertRepo;
    private final UserRepository userRepo; // Added this
    private final UserService userService;
    private final AlertService alertService; // Added this

    @PostMapping("/test")
    public String createTestAlerts() {
        // Get a test user from DB (id=2 just as example)
        User user = userRepo.findById(2L).orElseThrow();

        alertService.thresholdAlerts(user, "Food", 0.85); // should trigger THRESHOLD_80
        alertService.burnRateAlert(user, "Travel",
                BigDecimal.valueOf(500), BigDecimal.valueOf(10000),
                10, 30, BigDecimal.valueOf(8000)); // should trigger BURN_RATE
        alertService.spikeAlert(user, "Shopping",
                BigDecimal.valueOf(3000), BigDecimal.valueOf(5000)); // should trigger SPIKE
        alertService.underuseAlert(user, "Health");
        alertService.recurringAlert(user, "Netflix");
        alertService.goalAlert(user, "Congrats! You reached your savings goal.");
        alertService.achievement(user, "Fitness", "Achieved your monthly fitness target!");

        return "Test alerts created!";
    }

    @GetMapping
    public ResponseEntity<?> history(@RequestHeader("Authorization") String auth) {
        User user = userService.getCurrentUser(auth).orElseThrow();
        return ResponseEntity.ok(alertRepo.findByUserIdOrderByCreatedAtDesc(user.getId()));
    }
}
