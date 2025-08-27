package site.petful.snsservice.instagram.comment.repository;

import static site.petful.snsservice.instagram.comment.entity.QInstagramBannedWordEntity.instagramBannedWordEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import site.petful.snsservice.instagram.comment.entity.InstagramBannedWordEntity;

@RequiredArgsConstructor
public class InstagramBannedWordRepositoryImpl implements InstagramBannedWordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<InstagramBannedWordEntity> getBannedWord(Long instagramId, String keyword) {

        return queryFactory
            .selectFrom(instagramBannedWordEntity)
            .where(
                instagramBannedWordEntity.id.instagramId.eq(instagramId),
                keywordContains(keyword)
            )
            .fetch();
    }

    private BooleanExpression keywordContains(String keyword) {
        return keyword != null && !keyword.isBlank()
            ? instagramBannedWordEntity.id.word.containsIgnoreCase(keyword) : null;
    }
}
