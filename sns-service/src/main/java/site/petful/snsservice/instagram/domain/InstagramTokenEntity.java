package site.petful.snsservice.instagram.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "instagram_token")
@RequiredArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Setter
@Getter
public class InstagramTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    @Column(name = "token", length = 512)
    private String token;
    private LocalDateTime expireAt;
    @CreatedDate
    private LocalDateTime createdAt;


    public InstagramTokenEntity(Long userId, String token, Long expiresIn) {
        this.userId = userId;
        this.token = token;
        if (expiresIn == null) {
            expiresIn = 60 * 24 * 60 * 60L;
        }
        this.expireAt = LocalDateTime.now().plusSeconds(expiresIn);
    }

}
