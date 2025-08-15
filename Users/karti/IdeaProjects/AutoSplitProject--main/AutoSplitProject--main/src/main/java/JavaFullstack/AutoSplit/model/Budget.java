package JavaFullstack.AutoSplit.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="budgets",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","category","year","month"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Budget {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="user_id")
    private User user;

    @Column(nullable=false)
    private String category; // lowercase, trimmed

    @Column(nullable=false)
    private Integer year;

    @Column(nullable=false)
    private Integer month; // 1..12

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private BudgetType type; // PERCENTAGE or FIXED

    // If PERCENTAGE: percentOfIncome (0..100). If FIXED: fixedAmount > 0
    private BigDecimal percentOfIncome; // nullable
    private BigDecimal fixedAmount;     // nullable

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
