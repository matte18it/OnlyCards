package unical.enterpriceapplication.onlycards.application.data.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.User;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "invalidated_token")
public class InvalidatedToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Basic(optional = false)
    @Column(name = "expiry_date")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime expiryDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
}
