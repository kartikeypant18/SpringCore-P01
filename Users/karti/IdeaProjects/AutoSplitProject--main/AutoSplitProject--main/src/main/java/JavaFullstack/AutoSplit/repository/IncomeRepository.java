package JavaFullstack.AutoSplit.repository;

import JavaFullstack.AutoSplit.model.Income;
import JavaFullstack.AutoSplit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findByUser(User user);
    List<Income> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

}
