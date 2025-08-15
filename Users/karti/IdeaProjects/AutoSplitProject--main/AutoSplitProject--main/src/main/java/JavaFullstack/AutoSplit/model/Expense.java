package JavaFullstack.AutoSplit.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="expenses", indexes = {
        @Index(name="idx_expenses_user_date", columnList="user_id,expenseDate"),
        @Index(name="idx_expenses_user_cat_date", columnList="user_id,category,expenseDate")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Expense {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="user_id")
    private User user;

    @Column(nullable=false)
    private String category; // lowercase

    @Column(nullable=false, precision=18, scale=2)
    private BigDecimal amount;

    @Column(nullable=false)
    private LocalDate expenseDate;

    private String merchant; // for recurring detection (optional)
    private String note;

    private LocalDateTime createdAt = LocalDateTime.now();
}
