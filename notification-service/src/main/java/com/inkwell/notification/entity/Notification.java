package com.inkwell.notification.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer notificationId;
    @Column(nullable=false)
    private Integer recipientId;
    private Integer actorId;
    private String type;
    private String title;
    @Column(length=1000) private String message;
    private Integer relatedId;
    private String relatedType;
    private boolean isRead = false;
    private LocalDateTime createdAt;
}