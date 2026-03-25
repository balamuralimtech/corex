DROP TABLE IF EXISTS user_notification_receipt;
DROP TABLE IF EXISTS application_notification;

CREATE TABLE application_notification (
    id INT PRIMARY KEY AUTO_INCREMENT,
    message VARCHAR(1000) NOT NULL,
    created_by_user_id INT DEFAULT NULL,
    created_by_user_name VARCHAR(100) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_application_notification_user
        FOREIGN KEY (created_by_user_id) REFERENCES UserDetails(user_id)
);

CREATE TABLE user_notification_receipt (
    id INT PRIMARY KEY AUTO_INCREMENT,
    notification_id INT NOT NULL,
    user_id INT NOT NULL,
    seen_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_notification_receipt_notification
        FOREIGN KEY (notification_id) REFERENCES application_notification(id),
    CONSTRAINT fk_user_notification_receipt_user
        FOREIGN KEY (user_id) REFERENCES UserDetails(user_id),
    CONSTRAINT uk_user_notification_receipt UNIQUE (notification_id, user_id)
);
