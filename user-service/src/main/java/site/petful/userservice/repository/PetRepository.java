package site.petful.userservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import site.petful.userservice.entity.Pet;
import site.petful.userservice.entity.PetStarStatus;

public interface PetRepository extends JpaRepository<Pet, Long> {
    Page<Pet> findByPetStarStatus(PetStarStatus petStarStatus, Pageable pageable);
}
