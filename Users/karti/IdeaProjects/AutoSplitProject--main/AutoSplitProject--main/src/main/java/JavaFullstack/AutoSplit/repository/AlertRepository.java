package JavaFullstack.AutoSplit.repository;

import JavaFullstack.AutoSplit.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.*;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByUserIdOrderByCreatedAtDesc(Long userId);
}
