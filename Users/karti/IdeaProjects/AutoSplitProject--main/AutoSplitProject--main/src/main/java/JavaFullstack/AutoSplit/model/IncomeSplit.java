package JavaFullstack.AutoSplit.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "income_splits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonProperty("category")
    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(nullable = false)
    private Double percentage;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}