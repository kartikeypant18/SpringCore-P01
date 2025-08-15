package JavaFullstack.AutoSplit.service;

import JavaFullstack.AutoSplit.model.*;
import JavaFullstack.AutoSplit.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IncomeService {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private IncomeSplitRepository incomeSplitRepository;

    @Autowired
    private SplitTransactionRepository splitTransactionRepository;

    /**
     * Add an Income and automatically create per-category SplitTransaction rows
     */
    public Income addIncomeAndCreateSplits(User user, Double amount, String source) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be > 0");
        }

        // Persist Income
        Income income = Income.builder()
                .user(user)
                .amount(amount)
                .source(source != null ? source : "income")
                .createdAt(LocalDateTime.now())
                .build();
        income = incomeRepository.save(income);

        // Get saved splits (percentages) for this user
        List<IncomeSplit> splits = incomeSplitRepository.findByUser(user);
        if (splits == null || splits.isEmpty()) {
            // Option: choose to throw or just create a generic "Unallocated" tx
            // I'll throw so user knows to set splits first.
            throw new IllegalStateException("No income splits configured for user. Please set categories first.");
        }

        // Create SplitTransactions
        for (IncomeSplit split : splits) {
            double splitAmount = (amount * split.getPercentage()) / 100.0;
            SplitTransaction tx = SplitTransaction.builder()
                    .income(income)
                    .user(user)
                    .categoryName(split.getCategoryName())
                    .amount(splitAmount)
                    .createdAt(LocalDateTime.now())
                    .build();
            splitTransactionRepository.save(tx);
        }

        return income;
    }


    public List<SplitTransaction> getSplitHistory(User user) {
        return splitTransactionRepository.findByUser(user);
    }
}
