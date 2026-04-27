package com.inkwell.notification.listener;

import com.inkwell.notification.dto.NotificationDtos.SendNotificationRequest;
import com.inkwell.notification.entity.Notification.NotificationType;
import com.inkwell.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consumes events published by comment-service and post-service via RabbitMQ
 * and creates the appropriate in-app notifications.
 *
 * Features:
 * - Dead letter queue support for failed messages
 * - Retry with exponential backoff
 * - Comprehensive error logging
 * - Message tracking via delivery tags
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true")
public class NotificationEventListener {

    private final NotificationService notificationService;

    /**
     * Handles NEW_COMMENT and COMMENT_REPLY events from comment-service.
     * Failed messages are routed to DLQ for later analysis.
     */
    @RabbitListener(queues = "${app.rabbitmq.queue.comment-events}")
    public void handleCommentEvent(Map<String, Object> event,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("Processing comment event: deliveryTag={}, event={}", deliveryTag, event);
        
        try {
            String  type           = (String)  event.get("type");
            Integer commentAuthorId = (Integer) event.get("authorId");
            Integer postId         = (Integer) event.get("postId");
            Integer commentId      = (Integer) event.get("commentId");
            Object  parentId       = event.get("parentCommentId");

            if ("NEW_COMMENT".equals(type)) {
                log.debug("Processing NEW_COMMENT: commentId={}, postId={}, authorId={}",
                        commentId, postId, commentAuthorId);
                
                notificationService.send(SendNotificationRequest.builder()
                        .recipientId(postId)
                        .actorId(commentAuthorId)
                        .type(NotificationType.NEW_COMMENT)
                        .title("New comment on your post")
                        .message("Someone commented on your post.")
                        .relatedId(commentId)
                        .relatedType("COMMENT")
                        .build());
                
                log.info("Successfully created NEW_COMMENT notification: commentId={}", commentId);

            } else if ("COMMENT_REPLY".equals(type) && parentId != null
                    && !"null".equals(parentId.toString())) {
                Integer parentCommentId = Integer.parseInt(parentId.toString());
                log.debug("Processing COMMENT_REPLY: commentId={}, parentId={}, authorId={}",
                        commentId, parentCommentId, commentAuthorId);
                
                notificationService.send(SendNotificationRequest.builder()
                        .recipientId(parentCommentId)
                        .actorId(commentAuthorId)
                        .type(NotificationType.COMMENT_REPLY)
                        .title("Someone replied to your comment")
                        .message("You have a new reply.")
                        .relatedId(commentId)
                        .relatedType("COMMENT")
                        .build());
                
                log.info("Successfully created COMMENT_REPLY notification: commentId={}", commentId);
            }

        } catch (Exception e) {
            log.error("Failed to process comment event: deliveryTag={}, error={}", 
                    deliveryTag, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Handles POST_PUBLISHED events from post-service.
     * Failed messages are routed to DLQ.
     */
    @RabbitListener(queues = "${app.rabbitmq.queue.post-events}")
    public void handlePostPublishedEvent(Map<String, Object> event,
                                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("Processing post published event: deliveryTag={}, event={}", deliveryTag, event);
        
        try {
            Integer postId   = (Integer) event.get("postId");
            Integer authorId = (Integer) event.get("authorId");
            String  title    = (String)  event.get("title");

            log.info("Post published event received: postId={}, authorId={}, title={}",
                    postId, authorId, title);

        } catch (Exception e) {
            log.error("Failed to process post published event: deliveryTag={}, error={}",
                    deliveryTag, e.getMessage(), e);
            throw e;
        }
    }
}
