package JavaFullstack.AutoSplit.service;

import JavaFullstack.AutoSplit.model.*;
import JavaFullstack.AutoSplit.repository.StreakRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service @RequiredArgsConstructor
public class StreakService {
    private final StreakRepository repo;

    public void bumpIfGoodDay(User user, String category, boolean underBudgetToday){
        String cat = category==null?null:category.trim().toLowerCase();
        var s = repo.findByUserIdAndCategory(user.getId(), cat)
                .orElse(Streak.builder().user(user).category(cat).currentStreakDays(0).longestStreakDays(0).build());

        LocalDate today = LocalDate.now();
        if (s.getLastCheckDate()!=null && s.getLastCheckDate().isEqual(today)) return; // once per day

        if (underBudgetToday){
            s.setCurrentStreakDays(s.getCurrentStreakDays()+1);
            if (s.getCurrentStreakDays()>s.getLongestStreakDays()) s.setLongestStreakDays(s.getCurrentStreakDays());
        } else {
            s.setCurrentStreakDays(0);
        }
        s.setLastCheckDate(today);
        repo.save(s);
    }
}
