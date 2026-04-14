package com.inkwell.media.entity;

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
@Table(name = "media")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer mediaId;
	@Column(nullable=false) private Integer uploaderId;
    private String filename;
    private String originalName;
    @Column(nullable=false) private String url;
    private String mimeType;
    private Long sizeKb;
    private String altText;
    private Integer linkedPostId;
    private LocalDateTime uploadedAt;
    private boolean isDeleted = false;
}
