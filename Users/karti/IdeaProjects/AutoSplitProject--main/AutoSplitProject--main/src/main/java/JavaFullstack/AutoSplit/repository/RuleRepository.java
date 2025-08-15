package JavaFullstack.AutoSplit.repository;

import JavaFullstack.AutoSplit.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.*;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {
    Optional<Rule> findByKeyName(String keyName);
}