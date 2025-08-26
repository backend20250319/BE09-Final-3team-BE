package site.petful.snsservice.instagram.comment.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.snsservice.instagram.comment.dto.BannedWordResponseDto;
import site.petful.snsservice.instagram.comment.entity.InstagramBannedWordEntity;
import site.petful.snsservice.instagram.comment.repository.InstagramBannedWordRepository;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;
import site.petful.snsservice.instagram.profile.repository.InstagramProfileRepository;

@Service
@RequiredArgsConstructor
public class InstagramBannedWordService {

    private final InstagramBannedWordRepository instagramBannedWordRepository;
    private final InstagramProfileRepository instagramProfileRepository;


    @Transactional
    public void addBannedWord(Long instagramId, String word) {
        InstagramProfileEntity profile = instagramProfileRepository.findById(instagramId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 인스타그램 프로필입니다."));

        InstagramBannedWordEntity entity = new InstagramBannedWordEntity(
            null, profile, word);
        instagramBannedWordRepository.save(entity);

    }

    @Transactional
    public void deleteBannedWord(Long id) {
        if (!instagramBannedWordRepository.existsById(id)) {
            throw new NoSuchElementException("존재하지 않는 금지어입니다.");
        }

        instagramBannedWordRepository.deleteById(id);
    }

    public Set<String> getBannedWords(InstagramProfileEntity profile) {
        return instagramBannedWordRepository.findByInstagramProfile(profile).stream()
            .map(InstagramBannedWordEntity::getWord)
            .collect(Collectors.toSet());
    }


    public List<BannedWordResponseDto> getBannedWords(Long instagramId, String Keyword) {
        InstagramProfileEntity profile = instagramProfileRepository.findById(instagramId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 인스타그램 프로필입니다."));

        List<InstagramBannedWordEntity> bannedWords = instagramBannedWordRepository
            .getBannedWord(instagramId, Keyword);

        return bannedWords.stream()
            .map(bw -> new BannedWordResponseDto(bw.getId(), bw.getWord()))
            .collect(Collectors.toList());
    }

}
