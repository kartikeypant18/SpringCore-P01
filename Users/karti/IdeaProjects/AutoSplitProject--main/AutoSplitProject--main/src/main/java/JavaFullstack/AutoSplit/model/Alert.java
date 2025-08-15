package JavaFullstack.AutoSplit.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="alerts", indexes = {
        @Index(name="idx_alerts_user_created", columnList="user_id,createdAt")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Alert {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="user_id")
    private User user;

    private String category; // nullable for global alerts
    @Enumerated(EnumType.STRING)
    private AlertType type;
    private String severity; // INFO/WARN/CRITICAL (text for now)
    @Column(length=500)
    private String message;

    private Double metric; // e.g., utilization%, burn-rate-days-left, spike%
    private LocalDateTime createdAt = LocalDateTime.now();
}
