package com.inkwell.newsletter.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subscribers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscriber {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer subscriberId;
	@Column(unique = true, nullable = false)
	private String email;
	private Integer userId;
	private String fullName;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SubscriberStatus status;
	private LocalDateTime subscribedAt;
	private LocalDateTime unsubscribedAt;
	@Column(unique = true)
	private String token;
	private String preferences;

	public enum SubscriberStatus {
		PENDING, ACTIVE, UNSUBSCRIBED
	}
}
