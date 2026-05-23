CREATE TABLE IF NOT EXISTS application_feedback (
    id INT AUTO_INCREMENT PRIMARY KEY,
    subject VARCHAR(140) NOT NULL,
    message VARCHAR(4000) NOT NULL,
    created_by_user_id INT NOT NULL,
    created_by_user_name VARCHAR(100) NOT NULL,
    organization_id INT NULL,
    organization_name VARCHAR(150) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_application_feedback_created_at (created_at),
    INDEX idx_application_feedback_created_by_user_id (created_by_user_id),
    INDEX idx_application_feedback_organization_id (organization_id)
);
