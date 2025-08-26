package org.example.petservice.repository;

import org.example.petservice.entity.Pet;
import org.example.petservice.entity.PetStarStatus;
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
    
    Page<Pet> findByUserNo(Long userNo, Pageable pageable);
    
    Page<Pet> findByPetStarStatus(PetStarStatus status, Pageable pageable);
    
    @Query("SELECT p FROM Pet p WHERE p.userNo = :userNo AND p.petStarStatus = :status")
    List<Pet> findByUserNoAndPetStarStatus(@Param("userNo") Long userNo, @Param("status") PetStarStatus status);
    
    boolean existsByUserNoAndName(Long userNo, String name);
}
