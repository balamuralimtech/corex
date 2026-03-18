CREATE TABLE useractivities (
    activity_id INT AUTO_INCREMENT PRIMARY KEY,        -- Unique identifier for each activity
    user_id INT NOT NULL,                              -- ID of the user performing the activity
    activity_type VARCHAR(255) NOT NULL,               -- Type of activity (e.g., login, logout, view, update, etc.)
    activity_description TEXT,                         -- Optional detailed description of the activity
    ipaddress VARCHAR(45),                            -- IP address from which the activity was performed (IPv4 or IPv6)
    deviceinfo VARCHAR(255),                          -- Information about the device (e.g., browser, OS)
    locationinfo VARCHAR(255),                        -- Geolocation or region info if available
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,    -- Timestamp of when the activity occurred
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  -- Last update timestamp
);

INSERT INTO useractivities (user_id, activity_type, activity_description, ipaddress, deviceinfo, locationinfo, created_at, updated_at)
VALUES
-- Generate 50 dummy rows
(1, 'login', 'User logged in', '192.168.1.10', 'Chrome on Windows 10', 'New York, USA', NOW(), NOW()),
(2, 'logout', 'User logged out', '192.168.1.11', 'Safari on iOS', 'Los Angeles, USA', NOW(), NOW()),
(3, 'view', 'User viewed profile page', '192.168.1.12', 'Firefox on Ubuntu', 'London, UK', NOW(), NOW()),
(4, 'update', 'User updated profile', '192.168.1.13', 'Edge on Windows 11', 'Toronto, Canada', NOW(), NOW()),
(5, 'delete', 'User deleted a post', '192.168.1.14', 'Chrome on MacOS', 'Berlin, Germany', NOW(), NOW()),
(6, 'login', 'User logged in', '192.168.1.15', 'Firefox on Android', 'Mumbai, India', NOW(), NOW()),
(7, 'login', 'User logged in', '192.168.1.16', 'Chrome on Windows 10', 'Paris, France', NOW(), NOW()),
(8, 'logout', 'User logged out', '192.168.1.17', 'Safari on iOS', 'Sydney, Australia', NOW(), NOW()),
(9, 'view', 'User viewed home page', '192.168.1.18', 'Firefox on Ubuntu', 'Cape Town, South Africa', NOW(), NOW()),
(10, 'update', 'User updated settings', '192.168.1.19', 'Edge on Windows 11', 'Singapore', NOW(), NOW()),
(11, 'login', 'User logged in', '192.168.1.20', 'Chrome on Windows 10', 'New York, USA', NOW(), NOW()),
(12, 'logout', 'User logged out', '192.168.1.21', 'Safari on iOS', 'Los Angeles, USA', NOW(), NOW()),
(13, 'view', 'User viewed profile page', '192.168.1.22', 'Firefox on Ubuntu', 'London, UK', NOW(), NOW()),
(14, 'update', 'User updated profile', '192.168.1.23', 'Edge on Windows 11', 'Toronto, Canada', NOW(), NOW()),
(15, 'delete', 'User deleted a post', '192.168.1.24', 'Chrome on MacOS', 'Berlin, Germany', NOW(), NOW()),
(16, 'login', 'User logged in', '192.168.1.25', 'Firefox on Android', 'Mumbai, India', NOW(), NOW()),
(17, 'login', 'User logged in', '192.168.1.26', 'Chrome on Windows 10', 'Paris, France', NOW(), NOW()),
(18, 'logout', 'User logged out', '192.168.1.27', 'Safari on iOS', 'Sydney, Australia', NOW(), NOW()),
(19, 'view', 'User viewed home page', '192.168.1.28', 'Firefox on Ubuntu', 'Cape Town, South Africa', NOW(), NOW()),
(20, 'update', 'User updated settings', '192.168.1.29', 'Edge on Windows 11', 'Singapore', NOW(), NOW()),
(21, 'login', 'User logged in', '192.168.1.30', 'Chrome on Windows 10', 'New York, USA', NOW(), NOW()),
(22, 'logout', 'User logged out', '192.168.1.31', 'Safari on iOS', 'Los Angeles, USA', NOW(), NOW()),
(23, 'view', 'User viewed profile page', '192.168.1.32', 'Firefox on Ubuntu', 'London, UK', NOW(), NOW()),
(24, 'update', 'User updated profile', '192.168.1.33', 'Edge on Windows 11', 'Toronto, Canada', NOW(), NOW()),
(25, 'delete', 'User deleted a post', '192.168.1.34', 'Chrome on MacOS', 'Berlin, Germany', NOW(), NOW()),
(26, 'login', 'User logged in', '192.168.1.35', 'Firefox on Android', 'Mumbai, India', NOW(), NOW()),
(27, 'login', 'User logged in', '192.168.1.36', 'Chrome on Windows 10', 'Paris, France', NOW(), NOW()),
(28, 'logout', 'User logged out', '192.168.1.37', 'Safari on iOS', 'Sydney, Australia', NOW(), NOW()),
(29, 'view', 'User viewed home page', '192.168.1.38', 'Firefox on Ubuntu', 'Cape Town, South Africa', NOW(), NOW()),
(30, 'update', 'User updated settings', '192.168.1.39', 'Edge on Windows 11', 'Singapore', NOW(), NOW()),
(31, 'login', 'User logged in', '192.168.1.40', 'Chrome on Windows 10', 'New York, USA', NOW(), NOW()),
(32, 'logout', 'User logged out', '192.168.1.41', 'Safari on iOS', 'Los Angeles, USA', NOW(), NOW()),
(33, 'view', 'User viewed profile page', '192.168.1.42', 'Firefox on Ubuntu', 'London, UK', NOW(), NOW()),
(34, 'update', 'User updated profile', '192.168.1.43', 'Edge on Windows 11', 'Toronto, Canada', NOW(), NOW()),
(35, 'delete', 'User deleted a post', '192.168.1.44', 'Chrome on MacOS', 'Berlin, Germany', NOW(), NOW()),
(36, 'login', 'User logged in', '192.168.1.45', 'Firefox on Android', 'Mumbai, India', NOW(), NOW()),
(37, 'login', 'User logged in', '192.168.1.46', 'Chrome on Windows 10', 'Paris, France', NOW(), NOW()),
(38, 'logout', 'User logged out', '192.168.1.47', 'Safari on iOS', 'Sydney, Australia', NOW(), NOW()),
(39, 'view', 'User viewed home page', '192.168.1.48', 'Firefox on Ubuntu', 'Cape Town, South Africa', NOW(), NOW()),
(40, 'update', 'User updated settings', '192.168.1.49', 'Edge on Windows 11', 'Singapore', NOW(), NOW()),
(41, 'login', 'User logged in', '192.168.1.50', 'Chrome on Windows 10', 'New York, USA', NOW(), NOW()),
(42, 'logout', 'User logged out', '192.168.1.51', 'Safari on iOS', 'Los Angeles, USA', NOW(), NOW()),
(43, 'view', 'User viewed profile page', '192.168.1.52', 'Firefox on Ubuntu', 'London, UK', NOW(), NOW()),
(44, 'update', 'User updated profile', '192.168.1.53', 'Edge on Windows 11', 'Toronto, Canada', NOW(), NOW()),
(45, 'delete', 'User deleted a post', '192.168.1.54', 'Chrome on MacOS', 'Berlin, Germany', NOW(), NOW()),
(46, 'login', 'User logged in', '192.168.1.55', 'Firefox on Android', 'Mumbai, India', NOW(), NOW()),
(47, 'login', 'User logged in', '192.168.1.56', 'Chrome on Windows 10', 'Paris, France', NOW(), NOW()),
(48, 'logout', 'User logged out', '192.168.1.57', 'Safari on iOS', 'Sydney, Australia', NOW(), NOW()),
(49, 'view', 'User viewed home page', '192.168.1.58', 'Firefox on Ubuntu', 'Cape Town, South Africa', NOW(), NOW()),
(50, 'update', 'User updated settings', '192.168.1.59', 'Edge on Windows 11', 'Singapore', NOW(), NOW());

ALTER TABLE useractivities
    ADD COLUMN username VARCHAR(255) NOT NULL;

ALTER TABLE useractivities DROP COLUMN updated_at;