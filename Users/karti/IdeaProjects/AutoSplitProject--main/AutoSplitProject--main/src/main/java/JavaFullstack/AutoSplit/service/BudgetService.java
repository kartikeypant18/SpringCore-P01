package JavaFullstack.AutoSplit.service;

import JavaFullstack.AutoSplit.model.*;
import JavaFullstack.AutoSplit.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;

@Service @RequiredArgsConstructor
public class BudgetService {
    private final BudgetRepository budgetRepo;

    @Transactional
    public Budget upsertBudget(User user, String category, YearMonth ym, BudgetType type,
                               BigDecimal percent, BigDecimal fixed) {
        String cat = normalize(category);
        var opt = budgetRepo.findByUserIdAndCategoryAndYearAndMonth(user.getId(), cat, ym.getYear(), ym.getMonthValue());
        Budget b = opt.orElse(Budget.builder()
                .user(user).category(cat).year(ym.getYear()).month(ym.getMonthValue()).type(type).build());
        b.setType(type);
        b.setPercentOfIncome(type==BudgetType.PERCENTAGE ? percent : null);
        b.setFixedAmount(type==BudgetType.FIXED ? fixed : null);
        b.setUpdatedAt(java.time.LocalDateTime.now());
        return budgetRepo.save(b);
    }

    public List<Budget> getBudgets(User user, YearMonth ym){
        return budgetRepo.findByUserIdAndYearAndMonth(user.getId(), ym.getYear(), ym.getMonthValue());
    }

    private String normalize(String s){ return s==null?null:s.trim().toLowerCase(); }
}
