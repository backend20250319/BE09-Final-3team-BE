package site.petful.communityservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.petful.communityservice.connectNotice.dto.EventMessage;
import site.petful.communityservice.entity.Comment;
import site.petful.communityservice.entity.Post;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CommentEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishCommentCreatedEvent(Comment comment, Post post) {
        try {
            // 자기 자신에게는 알림을 보내지 않음
            if (comment.getUserId().equals(post.getUserId())) {
                log.info("🚫 [CommentEventPublisher] 자기 자신의 게시물에 댓글 - 알림 발행 건너뜀: commentUserId={}, postUserId={}", 
                    comment.getUserId(), post.getUserId());
                return;
            }

            // EventMessage 생성
            EventMessage eventMessage = createCommentEventMessage(comment, post);
            
            // RabbitMQ로 이벤트 발행
            rabbitTemplate.convertAndSend("notif.events", "community.activity", eventMessage);
            
            log.info("📤 [CommentEventPublisher] 댓글 이벤트 발행 성공: eventId={}, commentId={}, postId={}, targetUserId={}", 
                eventMessage.getEventId(), comment.getId(), post.getId(), post.getUserId());
                
        } catch (Exception e) {
            log.error("❌ [CommentEventPublisher] 댓글 이벤트 발행 실패: commentId={}, postId={}, error={}", 
                comment.getId(), post.getId(), e.getMessage(), e);
        }
    }

    private EventMessage createCommentEventMessage(Comment comment, Post post) {
        // Actor (댓글 작성자)
        EventMessage.Actor actor = new EventMessage.Actor();
        actor.setId(comment.getUserId());
        actor.setName("댓글 작성자"); // 실제로는 사용자 정보를 가져와야 함

        // Target (게시물 작성자)
        EventMessage.Target target = new EventMessage.Target();
        target.setUserId(post.getUserId().toString());
        target.setResourceId(post.getId());
        target.setResourceType("POST");

        // Attributes (추가 정보)
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("message", "새로운 댓글이 달렸습니다");
        attributes.put("commentId", comment.getId());
        attributes.put("commentContent", comment.getContent());
        attributes.put("postTitle", post.getTitle());
        attributes.put("postId", post.getId());

        // EventMessage 생성
        EventMessage eventMessage = new EventMessage();
        eventMessage.setEventId("comment-created-" + comment.getId());
        eventMessage.setType("notification.comment.created");
        eventMessage.setOccurredAt(Instant.now());
        eventMessage.setActor(actor);
        eventMessage.setTarget(target);
        eventMessage.setAttributes(attributes);
        eventMessage.setSchemaVersion(1);

        return eventMessage;
    }
}
