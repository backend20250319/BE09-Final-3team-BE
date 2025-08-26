package site.petful.snsservice.instagram.comment.service;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.snsservice.instagram.comment.entity.InstagramBannedWordEntity;
import site.petful.snsservice.instagram.comment.entity.InstagramBannedWordId;
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
            new InstagramBannedWordId(word, null), profile);
        instagramBannedWordRepository.save(entity);

    }

    @Transactional
    public void deleteBannedWord(Long instagramId, String word) {
        int res = instagramBannedWordRepository.deleteByInstagramProfileIdAndWord(instagramId,
            word);

        if (res == 0) {
            throw new NoSuchElementException("삭제할 금지어가 없습니다.");
        }
    }

    public Set<String> getBannedWords(InstagramProfileEntity profile) {
        return instagramBannedWordRepository.findByInstagramProfile(profile).stream()
            .map(InstagramBannedWordEntity::getWord)
            .collect(Collectors.toSet());
    }

}
