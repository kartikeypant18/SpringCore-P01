package JavaFullstack.AutoSplit.repository;

import JavaFullstack.AutoSplit.model.SplitTransaction;
import JavaFullstack.AutoSplit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SplitTransactionRepository extends JpaRepository<SplitTransaction, Long> {

    List<SplitTransaction> findByUser(User user);

    List<SplitTransaction> findByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM SplitTransaction s WHERE s.user.id = :userId AND FUNCTION('YEAR', s.createdAt) = :year AND FUNCTION('MONTH', s.createdAt) = :month")
    List<SplitTransaction> findByUserAndYearMonth(
            @Param("userId") Long userId,
            @Param("year") int year,
            @Param("month") int month
    );
}
