package site.petful.snsservice.instagram.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import site.petful.snsservice.instagram.dto.InstagramTokenResponse;

@Entity
@RequiredArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class InstagramToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String token;
    private LocalDateTime expireAt;
    @CreatedDate
    private LocalDateTime createdAt;


    public InstagramToken(Long userId, String token, Long expiresIn) {
        this.userId = userId;
        this.token = token;
        this.expireAt = LocalDateTime.now().plusSeconds(expiresIn);
    }

    public InstagramToken(Long userId, InstagramTokenResponse response) {
        this.userId = userId;
        this.expireAt = LocalDateTime.now().plusSeconds(response.getExpires_in());
        this.token = response.getAccess_token();
    }
}
