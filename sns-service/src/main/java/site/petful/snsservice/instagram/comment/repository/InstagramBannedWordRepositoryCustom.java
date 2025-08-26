package site.petful.snsservice.instagram.comment.repository;

import java.util.List;
import site.petful.snsservice.instagram.comment.entity.InstagramBannedWordEntity;

public interface InstagramBannedWordRepositoryCustom {

    List<InstagramBannedWordEntity> getBannedWord(Long instagramId,
        String keyword);
}
