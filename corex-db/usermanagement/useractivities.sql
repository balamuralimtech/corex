DROP TABLE IF EXISTS useractivities;

CREATE TABLE useractivities (
    activity_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    username VARCHAR(255) NOT NULL,
    activity_type VARCHAR(255) NOT NULL,
    activity_description TEXT,
    ipaddress VARCHAR(45),
    deviceinfo VARCHAR(255),
    locationinfo VARCHAR(255),
    session_id VARCHAR(128) DEFAULT NULL,
    termination_reason VARCHAR(64) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO useractivities (
    user_id,
    username,
    activity_type,
    activity_description,
    ipaddress,
    deviceinfo,
    locationinfo,
    session_id,
    termination_reason,
    created_at
)
VALUES
    (1, 'alice.johnson@example.com', 'login', 'User logged in', '192.168.1.10', 'Chrome on Windows 10', 'New York, USA', 'SESSION-1001', NULL, NOW()),
    (2, 'bob.smith@example.com', 'logout', 'User session ended: USER_LOGOUT', '192.168.1.11', 'Safari on iOS', 'Los Angeles, USA', 'SESSION-1002', 'USER_LOGOUT', NOW()),
    (3, 'cathy.lee@example.com', 'view', 'User viewed profile page', '192.168.1.12', 'Firefox on Ubuntu', 'London, UK', NULL, NULL, NOW()),
    (4, 'alice.johnson@example.com', 'update', 'User updated profile', '192.168.1.13', 'Edge on Windows 11', 'Toronto, Canada', 'SESSION-1003', NULL, NOW()),
    (5, 'bob.smith@example.com', 'delete', 'User deleted a post', '192.168.1.14', 'Chrome on MacOS', 'Berlin, Germany', 'SESSION-1004', NULL, NOW()),
    (6, 'cathy.lee@example.com', 'login', 'User logged in', '192.168.1.15', 'Firefox on Android', 'Mumbai, India', 'SESSION-1005', NULL, NOW());
