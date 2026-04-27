CREATE TABLE IF NOT EXISTS post_likes (
    post_like_id INT NOT NULL AUTO_INCREMENT,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (post_like_id),
    CONSTRAINT uk_post_like_post_user UNIQUE (post_id, user_id),
    INDEX idx_post_like_post (post_id),
    INDEX idx_post_like_user (user_id)
);
