package JavaFullstack.AutoSplit.repository;

import JavaFullstack.AutoSplit.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.*;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByUserIdAndCategoryAndYearAndMonth(Long userId, String category, Integer year, Integer month);
    List<Budget> findByUserIdAndYearAndMonth(Long userId, Integer year, Integer month);
}
