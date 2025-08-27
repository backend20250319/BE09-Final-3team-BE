package site.petful.petservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.petservice.dto.HistoryRequest;
import site.petful.petservice.dto.HistoryResponse;
import site.petful.petservice.entity.History;
import site.petful.petservice.entity.Pet;
import site.petful.petservice.repository.HistoryRepository;
import site.petful.petservice.repository.PetRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final PetRepository petRepository;

    // 활동이력 생성
    @Transactional
    public HistoryResponse createHistory(Long petNo, Long userNo, HistoryRequest request) {
        // 펫 존재 여부 및 소유권 확인
        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        if (!pet.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 반려동물의 활동이력을 생성할 권한이 없습니다.");
        }

        // 활동이력 생성
        History history = History.builder()
                .petNo(petNo)
                .historyStart(request.getHistoryStart())
                .historyEnd(request.getHistoryEnd())
                .content(request.getContent())
                .build();

        History savedHistory = historyRepository.save(history);
        return toHistoryResponse(savedHistory);
    }

    // 활동이력 조회
    public HistoryResponse getHistory(Long petNo, Long historyNo, Long userNo) {
        History history = historyRepository.findById(historyNo)
                .orElseThrow(() -> new IllegalArgumentException("활동이력을 찾을 수 없습니다: " + historyNo));

        // 소유권 확인
        if (!history.getPetNo().equals(petNo)) {
            throw new IllegalArgumentException("잘못된 반려동물 번호입니다.");
        }

        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        if (!pet.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 활동이력을 조회할 권한이 없습니다.");
        }

        return toHistoryResponse(history);
    }

    // 반려동물의 모든 활동이력 조회
    public List<HistoryResponse> getHistories(Long petNo, Long userNo) {
        // 펫 존재 여부 및 소유권 확인
        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        if (!pet.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 반려동물의 활동이력을 조회할 권한이 없습니다.");
        }

        List<History> histories = historyRepository.findByPetNo(petNo);
        return histories.stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    // 활동이력 수정
    @Transactional
    public HistoryResponse updateHistory(Long petNo, Long historyNo, Long userNo, HistoryRequest request) {
        History history = historyRepository.findById(historyNo)
                .orElseThrow(() -> new IllegalArgumentException("활동이력을 찾을 수 없습니다: " + historyNo));

        // 소유권 확인
        if (!history.getPetNo().equals(petNo)) {
            throw new IllegalArgumentException("잘못된 반려동물 번호입니다.");
        }

        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        if (!pet.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 활동이력을 수정할 권한이 없습니다.");
        }

        // 활동이력 정보 업데이트
        history.setHistoryStart(request.getHistoryStart());
        history.setHistoryEnd(request.getHistoryEnd());
        history.setContent(request.getContent());

        History updatedHistory = historyRepository.save(history);
        return toHistoryResponse(updatedHistory);
    }

    // 활동이력 삭제
    @Transactional
    public void deleteHistory(Long petNo, Long historyNo, Long userNo) {
        History history = historyRepository.findById(historyNo)
                .orElseThrow(() -> new IllegalArgumentException("활동이력을 찾을 수 없습니다: " + historyNo));

        // 소유권 확인
        if (!history.getPetNo().equals(petNo)) {
            throw new IllegalArgumentException("잘못된 반려동물 번호입니다.");
        }

        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        if (!pet.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 활동이력을 삭제할 권한이 없습니다.");
        }

        historyRepository.delete(history);
    }

    // DTO 변환 메서드
    private HistoryResponse toHistoryResponse(History history) {
        return HistoryResponse.builder()
                .historyNo(history.getHistoryNo())
                .historyStart(history.getHistoryStart())
                .historyEnd(history.getHistoryEnd())
                .content(history.getContent())
                .petNo(history.getPetNo())
                .createdAt(history.getCreatedAt())
                .updatedAt(history.getUpdatedAt())
                .build();
    }
}
