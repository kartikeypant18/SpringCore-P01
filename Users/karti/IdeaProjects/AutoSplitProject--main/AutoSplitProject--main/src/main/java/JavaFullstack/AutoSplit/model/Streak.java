package JavaFullstack.AutoSplit.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name="streaks",
        uniqueConstraints = @UniqueConstraint(columnNames={"user_id","category"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Streak {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="user_id")
    private User user;

    private String category; // null means global budget adherence

    private int currentStreakDays;
    private int longestStreakDays;

    private LocalDate lastCheckDate; // for day-by-day update
}
