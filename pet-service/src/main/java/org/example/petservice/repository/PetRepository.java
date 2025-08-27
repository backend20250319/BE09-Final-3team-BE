package org.example.petservice.repository;

import org.example.petservice.entity.Pet;
import org.example.petservice.entity.PetStarStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    
    List<Pet> findByUserNo(Long userNo);
    
    Page<Pet> findByPetStarStatus(PetStarStatus status, Pageable pageable);
    
    boolean existsByUserNoAndName(Long userNo, String name);
}
