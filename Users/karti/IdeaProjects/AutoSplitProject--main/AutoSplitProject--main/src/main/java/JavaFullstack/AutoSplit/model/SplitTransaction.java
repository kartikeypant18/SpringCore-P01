package JavaFullstack.AutoSplit.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "split_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SplitTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to Income (so we can trace origin)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "income_id")
    private Income income;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Automatically set createdAt before persisting
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
