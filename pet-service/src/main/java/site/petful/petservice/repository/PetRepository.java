package site.petful.petservice.repository;

import site.petful.petservice.entity.Pet;
import site.petful.petservice.entity.PetStarStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    
    List<Pet> findByUserNo(Long userNo);
    
    Page<Pet> findByPetStarStatus(PetStarStatus status, Pageable pageable);
    
    boolean existsByUserNoAndName(Long userNo, String name);
    
    // 펫스타 전체 조회 (ACTIVE 상태인 펫들)
    List<Pet> findByIsPetStarTrue();
    
    // petNos 리스트로 펫 조회
    @Query("SELECT p FROM Pet p WHERE p.petNo IN :petNos")
    List<Pet> findByPetNos(@Param("petNos") List<Long> petNos);
    
    // 유효한 userNo를 가진 펫스타 신청 목록 조회 (userNo가 null이 아니고 0보다 큰 경우)
    @Query("SELECT p FROM Pet p WHERE p.petStarStatus = :status AND p.userNo IS NOT NULL AND p.userNo > 0")
    Page<Pet> findByPetStarStatusWithValidUser(@Param("status") PetStarStatus status, Pageable pageable);
}
