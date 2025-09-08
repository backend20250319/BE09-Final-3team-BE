package site.petful.petservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.petful.petservice.client.InstagramProfileClient;
import site.petful.petservice.dto.InstagramProfileInfo;
import site.petful.petservice.dto.PetRequest;
import site.petful.petservice.dto.PetResponse;
import site.petful.petservice.entity.Pet;
import site.petful.petservice.entity.PetStarStatus;
import site.petful.petservice.repository.PetRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private InstagramProfileClient instagramProfileClient;

    @InjectMocks
    private PetService petService;

    private Pet testPet;
    private InstagramProfileInfo testInstagramProfile;

    @BeforeEach
    void setUp() {
        // 테스트용 Pet 엔티티 생성
        testPet = Pet.builder()
                .petNo(1L)
                .userNo(1L)
                .name("멍멍이")
                .type("골든리트리버")
                .age(3L)
                .gender("M")
                .weight(25.5f)
                .snsProfileNo(123456789L)  // Instagram 프로필 ID
                .isPetStar(false)
                .petStarStatus(PetStarStatus.NONE)
                .build();

        // 테스트용 Instagram 프로필 정보 생성
        testInstagramProfile = InstagramProfileInfo.builder()
                .id(123456789L)
                .username("my_dog_insta")
                .name("멍멍이 공식계정")
                .profilePictureUrl("https://example.com/profile.jpg")
                .followersCount(1000L)
                .followsCount(500L)
                .mediaCount(50L)
                .autoDelete(false)
                .build();
    }

    @Test
    void 반려동물_조회시_Instagram_프로필_정보가_포함되어야_한다() {
        // Given
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(instagramProfileClient.getProfile(123456789L)).thenReturn(testInstagramProfile);

        // When
        PetResponse response = petService.getPetById(1L);

        // Then
        assertThat(response.getPetNo()).isEqualTo(1L);
        assertThat(response.getSnsProfileNo()).isEqualTo(123456789L);
        assertThat(response.getInstagramProfile()).isNotNull();
        assertThat(response.getInstagramProfile().getId()).isEqualTo(123456789L);
        assertThat(response.getInstagramProfile().getUsername()).isEqualTo("my_dog_insta");
        assertThat(response.getInstagramProfile().getFollowersCount()).isEqualTo(1000L);
    }

    @Test
    void Instagram_프로필이_연결되지_않은_반려동물은_Instagram_정보가_null이어야_한다() {
        // Given
        testPet.setSnsProfileNo(null);
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));

        // When
        PetResponse response = petService.getPetById(1L);

        // Then
        assertThat(response.getPetNo()).isEqualTo(1L);
        assertThat(response.getSnsProfileNo()).isNull();
        assertThat(response.getInstagramProfile()).isNull();
    }

    @Test
    void Instagram_프로필_조회_실패시_Instagram_정보가_null이어야_한다() {
        // Given
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(instagramProfileClient.getProfile(123456789L))
                .thenThrow(new RuntimeException("Instagram API 호출 실패"));

        // When
        PetResponse response = petService.getPetById(1L);

        // Then
        assertThat(response.getPetNo()).isEqualTo(1L);
        assertThat(response.getSnsProfileNo()).isEqualTo(123456789L);
        assertThat(response.getInstagramProfile()).isNull();
    }
}
