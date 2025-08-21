package site.petful.mypageservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.petful.mypageservice.entity.UserBasic;

import java.util.Optional;

@Repository
public interface UserBasicRepository extends JpaRepository<UserBasic, Long> {
    
    Optional<UserBasic> findByEmail(String email);
    
    boolean existsByEmail(String email);
}
