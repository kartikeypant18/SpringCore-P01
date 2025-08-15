package JavaFullstack.AutoSplit.service;


import JavaFullstack.AutoSplit.model.IncomeSplit;
import JavaFullstack.AutoSplit.model.User;
import JavaFullstack.AutoSplit.repository.IncomeSplitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class IncomeSplitService {

    @Autowired
    private IncomeSplitRepository incomeSplitRepository;

    public List<IncomeSplit> getIncomeSplitsByUser(User user) {
        return incomeSplitRepository.findByUser(user);
    }

    // inside IncomeSplitService class (replace existing saveIncomeSplits)
    public List<IncomeSplit> saveIncomeSplits(User user, List<IncomeSplit> splits) {
        if (splits == null || splits.isEmpty()) {
            throw new IllegalArgumentException("Splits must not be empty");
        }

        // validate duplicates (case-insensitive)
        Set<String> cats = new HashSet<>();
        for (IncomeSplit s : splits) {
            String c = s.getCategoryName() == null ? "" : s.getCategoryName().trim().toLowerCase();
            if (c.isEmpty()) throw new IllegalArgumentException("Category name cannot be empty");
            if (!cats.add(c)) {
                throw new IllegalArgumentException("Duplicate category: " + s.getCategoryName());
            }
            if (s.getPercentage() == null || s.getPercentage() < 0) {
                throw new IllegalArgumentException("Invalid percentage for " + s.getCategoryName());
            }
        }

        // validate sum to (approx) 100%
        double sum = splits.stream().mapToDouble(IncomeSplit::getPercentage).sum();
        double EPS = 0.001;
        if (Math.abs(sum - 100.0) > EPS) {
            throw new IllegalArgumentException("Percentages must sum to 100. Found: " + sum);
        }

        // Overwrite behavior: delete existing splits then save new ones
        List<IncomeSplit> existing = incomeSplitRepository.findByUser(user);
        if (!existing.isEmpty()) {
            incomeSplitRepository.deleteAll(existing);
        }

        LocalDateTime now = LocalDateTime.now();
        splits.forEach(split -> {
            split.setUser(user);
            split.setCreatedAt(now);
            split.setUpdatedAt(now);
        });

        return incomeSplitRepository.saveAll(splits);
    }

}
