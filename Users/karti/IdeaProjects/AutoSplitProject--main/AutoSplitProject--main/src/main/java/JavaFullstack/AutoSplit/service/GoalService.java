package JavaFullstack.AutoSplit.service;

import JavaFullstack.AutoSplit.model.*;
import JavaFullstack.AutoSplit.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service @RequiredArgsConstructor
public class GoalService {
    private final GoalRepository goalRepo;

    public Goal createOrUpdate(User user, String name, BigDecimal target, BigDecimal saved, LocalDate targetDate){
        Goal g = goalRepo.findByUserId(user.getId()).stream()
                .filter(x -> x.getName().equalsIgnoreCase(name)).findFirst()
                .orElse(Goal.builder().user(user).name(name).targetAmount(target).savedAmount(saved).targetDate(targetDate).build());
        g.setTargetAmount(target);
        g.setSavedAmount(saved);
        g.setTargetDate(targetDate);
        g.setUpdatedAt(java.time.LocalDateTime.now());
        return goalRepo.save(g);
    }

    public String suggestion(Goal g, BigDecimal extraPerDay){
        if (g.getTargetDate()==null) return "Set a target date to get time-based suggestions.";
        long days = ChronoUnit.DAYS.between(java.time.LocalDate.now(), g.getTargetDate());
        if (days <= 0) return "Target date reached. Consider adjusting goal.";
        BigDecimal remaining = g.getTargetAmount().subtract(g.getSavedAmount());
        if (remaining.signum()<=0) return "Goal achieved!";
        BigDecimal neededPerDay = remaining.divide(java.math.BigDecimal.valueOf(days), 2, java.math.RoundingMode.HALF_UP);
        return "Save ~" + neededPerDay + "/day to hit your \""+g.getName()+"\" goal by "+g.getTargetDate()+".";
    }
    public List<Goal> getGoalsByUser(Long userId) {
        return goalRepo.findByUserId(userId);
    }
}
