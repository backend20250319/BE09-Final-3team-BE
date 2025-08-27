package site.petful.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.petful.userservice.entity.UserProfile;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    /**
     * 사용자 번호로 프로필 조회
     */
    Optional<UserProfile> findByUser_UserNo(Long userNo);
}

