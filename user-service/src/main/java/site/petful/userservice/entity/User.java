package site.petful.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_no")
    private Long userNo;
    
    @Column(name = "user_id", unique = true, nullable = false)
    private String email; // email이 userId 역할
    
    @Column(nullable = false, length = 255)
    private String password;
    
    @Column(nullable = false, length = 30)
    private String name;
    
    @Column(nullable = false, length = 255)
    private String nickname;
    
    @Column(nullable = false, length = 15)
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private Role userType;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "road_address", length = 255)
    private String roadAddress;
    
    @Column(name = "detail_address", length = 255)
    private String detailAddress;
    
    @Column(name = "birth_year")
    private Integer birthYear;
    
    @Column(name = "birth_month")
    private Integer birthMonth;
    
    @Column(name = "birth_day")
    private Integer birthDay;
    
    @Column(name = "email_verified")
    private Boolean emailVerified = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "image_no")
    private Long imageNo;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (emailVerified == null) {
            emailVerified = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + userType.name()));
    }
    
    @Override
    public String getUsername() {
        return email; // email을 username으로 사용
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return isActive;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return isActive;
    }
    
    @Override
    public boolean isEnabled() {
        return isActive;
    }

    public void suspend(){
        isActive = false;
    }
}
