DROP TABLE IF EXISTS UserDetails;

CREATE TABLE UserDetails (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    user_name VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email_id VARCHAR(255) UNIQUE NOT NULL,
    user_type VARCHAR(32) NOT NULL DEFAULT 'GENERAL_USER',
    contact VARCHAR(15),
    role_id INT NOT NULL,
    organization_id INT,
    branch_id INT,
    country_id MEDIUMINT UNSIGNED,
    state_id MEDIUMINT UNSIGNED,
    city_id MEDIUMINT UNSIGNED,
    address TEXT,
    access_right_id INT NOT NULL,
    status_id INT NOT NULL,
    account_disabled BOOLEAN NOT NULL DEFAULT FALSE,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    last_password_change TIMESTAMP DEFAULT NULL,
    last_successful_login TIMESTAMP DEFAULT NULL,
    last_seen_at TIMESTAMP DEFAULT NULL,
    last_logout_at TIMESTAMP DEFAULT NULL,
    last_session_id VARCHAR(128) DEFAULT NULL,
    profile_image LONGBLOB DEFAULT NULL,
    profile_image_content_type VARCHAR(100) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES Roles(id),
    CONSTRAINT fk_user_organization FOREIGN KEY (organization_id) REFERENCES Organizations(id),
    CONSTRAINT fk_user_branch FOREIGN KEY (branch_id) REFERENCES Branches(id),
    CONSTRAINT fk_user_country FOREIGN KEY (country_id) REFERENCES Countries(id),
    CONSTRAINT fk_user_state FOREIGN KEY (state_id) REFERENCES States(id),
    CONSTRAINT fk_user_city FOREIGN KEY (city_id) REFERENCES Cities(id)
);

INSERT INTO UserDetails (
    user_name,
    password,
    email_id,
    user_type,
    contact,
    role_id,
    organization_id,
    branch_id,
    country_id,
    state_id,
    city_id,
    address,
    access_right_id,
    status_id,
    created_at,
    updated_at
)
SELECT
    'admin',
    '$2a$12$vBIeMT2se0g.YOjyhMtwfuMihq3pEYy5Dj0kLXv/F6FgSweP0S9M.',
    'admin@corex.local',
    'GENERAL_USER',
    '1234567890',
    r.id,
    1,
    1,
    101,
    201,
    301,
    '123 Elm Street',
    1,
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM Roles r
WHERE r.role_name = 'Admin';

INSERT INTO Roles (role_name, created_at, updated_at)
SELECT 'Application Admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM Roles WHERE role_name = 'Application Admin'
);

INSERT INTO UserDetails (
    user_name,
    password,
    email_id,
    user_type,
    contact,
    role_id,
    organization_id,
    branch_id,
    country_id,
    state_id,
    city_id,
    address,
    access_right_id,
    status_id,
    created_at,
    updated_at
)
SELECT
    'appadmin',
    '$2a$12$4OBMzHmxjAjGA/De1r7meufQ7xELIQkoB9QaVDwI/i/9rCZUgr7Za',
    'appadmin@example.com',
    'APPLICATION_ADMIN',
    '9999999999',
    r.id,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    1,
    3,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM Roles r
WHERE r.role_name = 'Application Admin';
