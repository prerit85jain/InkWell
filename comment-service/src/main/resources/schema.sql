-- Comment Likes table for tracking which users liked which comments
CREATE TABLE IF NOT EXISTS comment_likes (
    comment_like_id INT NOT NULL AUTO_INCREMENT,
    comment_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (comment_like_id),
    CONSTRAINT uk_comment_like_comment_user UNIQUE (comment_id, user_id),
    INDEX idx_comment_like_comment (comment_id),
    INDEX idx_comment_like_user (user_id)
);