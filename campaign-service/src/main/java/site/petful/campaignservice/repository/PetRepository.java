package site.petful.campaignservice.repository;

import org.springframework.stereotype.Component;
import site.petful.campaignservice.dto.campaign.PetResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class PetRepository {

    private static final Map<Long, PetResponse> pets = new HashMap<>();

    static {
        pets.put(1L, new PetResponse(1L, "버디", "골든리트리버", 3, 'F', false, 1L));
        pets.put(2L, new PetResponse(2L, "초코", "말티즈", 2, 'M', false, 1L));
        pets.put(3L, new PetResponse(3L, "루나", "샴 고양이", 5, 'F', false, 1L));
    }

    public PetResponse findByPetNo(Long petNo) {
        return pets.get(petNo);
    }

    public List<PetResponse> findByPetNos(List<Long> petNos) {
        return petNos.stream()
                .map(pets::get)
                .collect(Collectors.toList());
    }

    public List<PetResponse> findByUserNo(Long userNo) {
        return pets.values().stream()
                .filter(pet -> pet.getUserNo().equals(userNo))
                .collect(Collectors.toList());
    }
}
