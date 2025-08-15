package JavaFullstack.AutoSplit.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    // Store only hashed password in DB
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // Temporary field for receiving plain password in JSON
    @Transient
    private String password;
}
