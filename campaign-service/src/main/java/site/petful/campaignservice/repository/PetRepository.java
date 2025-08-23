package site.petful.campaignservice.repository;

import org.springframework.stereotype.Component;
import site.petful.campaignservice.dto.campaign.PetResponse;

import java.util.HashMap;
import java.util.Map;

@Component
public class PetRepository {

    private static final Map<Long, PetResponse> pets = new HashMap<>();

    static {
        pets.put(1L, new PetResponse(1L, "버디", "골든리트리버", 3, 'F', false));
        pets.put(2L, new PetResponse(2L, "초코", "말티즈", 2, 'M', false));
    }

    public PetResponse findByPetNo(Long petNo) {
        return pets.get(petNo);
    }
}
