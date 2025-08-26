package site.petful.userservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import site.petful.userservice.admin.dto.PetStarResponse;
import site.petful.userservice.domain.Pet;
import site.petful.userservice.domain.PetStarStatus;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {
    Page<Pet> findByPetStarStatus(PetStarStatus petStarStatus, Pageable pageable);
}
