package unical.enterpriceapplication.onlycards.application.data.entities.orders;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
public class Orders {
    @Id
    @UuidGenerator
    @Column(unique = true, nullable = false, updatable = false, columnDefinition="uuid")
    private UUID id;

    @Column(nullable = false)
    private String vendorEmail;

    @Column(nullable = false)
    @Enumerated
    private Status status;

    @Column()
    @CreatedDate
    private LocalDate addDate;

    @Column()
    @LastModifiedDate
    private LocalDateTime modifyDate;

    @Column()
    @LastModifiedBy
    private String userLastEdit;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL)
    private List<Transactions> transactions;
}
