package site.petful.petservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import site.petful.petservice.client.InstagramProfileClient;
import site.petful.petservice.dto.InstagramProfileInfo;
import site.petful.petservice.dto.PetRequest;
import site.petful.petservice.entity.Pet;
import site.petful.petservice.entity.PetStarStatus;
import site.petful.petservice.repository.PetRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
class PetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InstagramProfileClient instagramProfileClient;

    @Autowired
    private PetRepository petRepository;

    @Test
    void Instagram_프로필_목록_조회_테스트() throws Exception {
        // Given
        List<InstagramProfileInfo> mockProfiles = Arrays.asList(
                InstagramProfileInfo.builder()
                        .id(123456789L)
                        .username("my_dog_insta")
                        .name("멍멍이 계정")
                        .followersCount(1000L)
                        .build(),
                InstagramProfileInfo.builder()
                        .id(987654321L)
                        .username("my_cat_insta")
                        .name("야옹이 계정")
                        .followersCount(500L)
                        .build()
        );

        when(instagramProfileClient.getProfilesByUser(1L)).thenReturn(mockProfiles);

        // When & Then
        mockMvc.perform(get("/instagram/profiles")
                        .header("X-User-No", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(123456789))
                .andExpect(jsonPath("$.data[0].username").value("my_dog_insta"))
                .andExpect(jsonPath("$.data[1].id").value(987654321))
                .andExpect(jsonPath("$.data[1].username").value("my_cat_insta"));
    }

    @Test
    void 반려동물_등록_및_조회_테스트() throws Exception {
        // Given
        PetRequest petRequest = PetRequest.builder()
                .name("테스트멍멍이")
                .type("골든리트리버")
                .age(3L)
                .gender("M")
                .weight(25.5f)
                .snsProfileNo(123456789L)
                .build();

        InstagramProfileInfo mockProfile = InstagramProfileInfo.builder()
                .id(123456789L)
                .username("test_dog_insta")
                .name("테스트 멍멍이 계정")
                .followersCount(1000L)
                .followsCount(500L)
                .mediaCount(50L)
                .autoDelete(false)
                .build();

        when(instagramProfileClient.getProfile(123456789L)).thenReturn(mockProfile);

        // When & Then - 반려동물 등록
        String response = mockMvc.perform(post("/pets")
                        .header("X-User-No", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(petRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("테스트멍멍이"))
                .andExpect(jsonPath("$.data.snsProfileNo").value(123456789))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 등록된 펫의 ID 추출 (실제 구현에서는 응답에서 petNo를 가져와야 함)
        // 여기서는 간단히 1L을 사용
        Long petNo = 1L;

        // When & Then - 반려동물 조회 (Instagram 프로필 정보 포함)
        mockMvc.perform(get("/pets/{petNo}", petNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.petNo").value(petNo))
                .andExpect(jsonPath("$.data.snsProfileNo").value(123456789))
                .andExpect(jsonPath("$.data.instagramProfile").exists())
                .andExpect(jsonPath("$.data.instagramProfile.id").value(123456789))
                .andExpect(jsonPath("$.data.instagramProfile.username").value("test_dog_insta"))
                .andExpect(jsonPath("$.data.instagramProfile.followersCount").value(1000));
    }
}
