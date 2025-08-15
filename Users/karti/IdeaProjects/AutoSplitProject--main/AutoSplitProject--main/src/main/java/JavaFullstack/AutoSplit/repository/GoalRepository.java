package JavaFullstack.AutoSplit.repository;

import JavaFullstack.AutoSplit.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.*;


public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUserId(Long userId);
}

