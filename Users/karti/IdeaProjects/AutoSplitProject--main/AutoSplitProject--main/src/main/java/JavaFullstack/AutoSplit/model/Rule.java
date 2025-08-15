package JavaFullstack.AutoSplit.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="rules", uniqueConstraints = @UniqueConstraint(columnNames={"keyName"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Rule {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String keyName; // e.g., "threshold.60", "spike.percent", "underuse.percent"

    @Column(nullable=false)
    private String value;   // store numeric or string messages; parse as needed

    private String description;
}
