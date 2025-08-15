package JavaFullstack.AutoSplit.service;

import JavaFullstack.AutoSplit.model.*;
import JavaFullstack.AutoSplit.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service @RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepo;

    @Transactional
    public Expense logExpense(User user, String category, BigDecimal amount, LocalDate date, String merchant, String note){
        Expense e = Expense.builder()
                .user(user)
                .category(category.trim().toLowerCase())
                .amount(amount)
                .expenseDate(date)
                .merchant(merchant)
                .note(note)
                .createdAt(LocalDateTime.now())
                .build();
        return expenseRepo.save(e);
    }
}
