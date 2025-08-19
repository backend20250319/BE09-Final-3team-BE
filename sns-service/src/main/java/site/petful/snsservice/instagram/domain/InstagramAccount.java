package site.petful.snsservice.instagram.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Entity
@EnableJpaAuditing
public class InstagramAccount {

    @Id
    private Long id;
    private String userName;
}
