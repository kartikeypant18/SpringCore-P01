package JavaFullstack.AutoSplit.repository;

import JavaFullstack.AutoSplit.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.*;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("select e from Expense e where e.user.id=:userId and e.expenseDate between :start and :end")
    List<Expense> findByUserAndDateRange(Long userId, LocalDate start, LocalDate end);

    @Query("select e.category, sum(e.amount) from Expense e where e.user.id=:userId and e.expenseDate between :start and :end group by e.category")
    List<Object[]> sumByCategory(Long userId, LocalDate start, LocalDate end);

    @Query("select date(e.expenseDate), sum(e.amount) from Expense e where e.user.id=:userId and e.category=:category and e.expenseDate between :start and :end group by e.expenseDate")
    List<Object[]> dailySums(Long userId, String category, LocalDate start, LocalDate end);

    @Query("select e.merchant, count(e) from Expense e where e.user.id=:userId and e.expenseDate between :start and :end group by e.merchant having count(e) >= :minCount")
    List<Object[]> recurringMerchants(Long userId, LocalDate start, LocalDate end, int minCount);
}
