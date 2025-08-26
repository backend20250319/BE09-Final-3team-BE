package site.petful.snsservice.instagram.comment.repository;

import static site.petful.snsservice.instagram.comment.entity.QInstagramCommentEntity.instagramCommentEntity;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import site.petful.snsservice.instagram.comment.dto.CommentSearchCondition;
import site.petful.snsservice.instagram.comment.entity.InstagramCommentEntity;
import site.petful.snsservice.instagram.comment.entity.Sentiment;


@RequiredArgsConstructor
public class InstagramCommentRepositoryImpl implements InstagramCommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<InstagramCommentEntity> searchComments(Long instagramId,
        CommentSearchCondition condition, Pageable pageable) {

        // 1. 데이터 조회 쿼리
        List<InstagramCommentEntity> content = queryFactory
            .selectFrom(instagramCommentEntity)
            .where(
                instagramCommentEntity.instagramProfile.id.eq(instagramId),
                isDeletedEq(condition.getIsDeleted()),
                sentimentEq(condition.getSentiment()),
                keywordContains(condition.getKeyword())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(getOrderSpecifiers(pageable.getSort()))
            .fetch();

        Long total = queryFactory
            .select(instagramCommentEntity.count())
            .from(instagramCommentEntity)
            .where(
                instagramCommentEntity.instagramProfile.id.eq(instagramId),
                isDeletedEq(condition.getIsDeleted()),
                sentimentEq(condition.getSentiment()),
                keywordContains(condition.getKeyword())
            )
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        if (sort.isEmpty()) {
            orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, instagramCommentEntity.timestamp));
            return orderSpecifiers.toArray(new OrderSpecifier[0]);
        }

        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            String property = order.getProperty();

            switch (property) {
                case "timestamp":
                    orderSpecifiers.add(
                        new OrderSpecifier<>(direction, instagramCommentEntity.timestamp));
                    break;
                case "sentiment":
                    orderSpecifiers.add(
                        new OrderSpecifier<>(direction, instagramCommentEntity.sentiment));
                    break;
                default:
                    break;
            }
        }

        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }


    private BooleanExpression isDeletedEq(Boolean isDeleted) {
        return isDeleted != null ? instagramCommentEntity.isDeleted.eq(isDeleted) : null;
    }

    private BooleanExpression sentimentEq(Sentiment sentiment) {
        return sentiment != null ? instagramCommentEntity.sentiment.eq(sentiment) : null;
    }

    private BooleanExpression keywordContains(String keyword) {
        return keyword != null && !keyword.isBlank()
            ? instagramCommentEntity.text.containsIgnoreCase(keyword) : null;
    }
}