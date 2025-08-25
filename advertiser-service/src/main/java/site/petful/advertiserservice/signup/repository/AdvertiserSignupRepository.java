package site.petful.advertiserservice.signup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.petful.advertiserservice.signup.entity.AdvertiserSignup;

import java.util.Optional;

public interface AdvertiserSignupRepository extends JpaRepository<AdvertiserSignup, Long> {

    Optional<AdvertiserSignup> findByAdvertiserNo(Long advertiserNo);
    
    boolean existsByUserId(String userId);
    
    boolean existsByAdvertiserNo(Long advertiserNo);
}

