package JavaFullstack.AutoSplit.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="goals")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Goal {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="user_id")
    private User user;

    @Column(nullable=false)
    private String name; // e.g., "vacation"

    @Column(precision=18, scale=2, nullable=false)
    private BigDecimal targetAmount;

    @Column(precision=18, scale=2, nullable=false)
    private BigDecimal savedAmount; // cumulative; can be derived from transfers, but store for speed

    private LocalDate targetDate; // for projection/suggestions

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
