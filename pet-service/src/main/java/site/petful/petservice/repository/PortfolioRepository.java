package site.petful.petservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.petful.petservice.entity.Portfolio;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    
    // 펫 번호로 포트폴리오 조회
    Optional<Portfolio> findByPetNo(Long petNo);
    
    // 사용자 번호로 포트폴리오 목록 조회 (JOIN으로 수정)
    @Query("SELECT p FROM Portfolio p JOIN Pet pet ON p.petNo = pet.petNo WHERE pet.userNo = :userNo")
    List<Portfolio> findByUserNo(@Param("userNo") Long userNo);
    
    // 임시저장된 포트폴리오 조회
    List<Portfolio> findByIsSavedTrue();
    
    // 펫 번호로 포트폴리오 존재 여부 확인
    boolean existsByPetNo(Long petNo);
}
