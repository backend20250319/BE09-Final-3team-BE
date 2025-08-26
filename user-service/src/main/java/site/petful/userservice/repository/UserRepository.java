package site.petful.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.petful.userservice.entity.User;

import java.util.Optional;


@Repository // 없어도 Spring Data가 자동 등록하지만 명시해도 OK
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

}
