package JavaFullstack.AutoSplit.repository;

import JavaFullstack.AutoSplit.model.IncomeSplit;
import JavaFullstack.AutoSplit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncomeSplitRepository extends JpaRepository<IncomeSplit, Long> {
    List<IncomeSplit> findByUser(User user);
}