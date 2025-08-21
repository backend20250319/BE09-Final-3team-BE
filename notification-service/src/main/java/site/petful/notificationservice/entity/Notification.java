package site.petful.notificationservice.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="notification")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "noticifation_no")
    private Long id;

    @Column(name="user_no",nullable = false)
    private Long userId;

    @Column(nullable = false , length = 32)
    private String type;

    @Column(length = 120)
    private String title;

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @Column(name = "link_url", length = 512)
    private String linkUrl;

    @Column(name = "is_hidden", nullable = false)
    private Boolean hidden = false;              // TINYINT(1)

    @Column(name = "hidden_at")
    private LocalDateTime hiddenAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public void hide() {
        if (Boolean.TRUE.equals(this.hidden)) return;
        this.hidden = true;
        this.hiddenAt = LocalDateTime.now();
    }

}
