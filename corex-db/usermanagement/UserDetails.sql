DROP TABLE IF EXISTS UserDetails;

CREATE TABLE UserDetails (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    user_name VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email_id VARCHAR(255) UNIQUE NOT NULL,
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
    last_password_change TIMESTAMP DEFAULT NULL,
    last_successful_login TIMESTAMP DEFAULT NULL,
    last_seen_at TIMESTAMP DEFAULT NULL,
    last_logout_at TIMESTAMP DEFAULT NULL,
    last_session_id VARCHAR(128) DEFAULT NULL,
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
    last_password_change,
    last_successful_login,
    last_seen_at,
    last_logout_at,
    last_session_id
)
VALUES
    ('admin', '$2a$12$vBIeMT2se0g.YOjyhMtwfuMihq3pEYy5Dj0kLXv/F6FgSweP0S9M.', 'alice.johnson@example.com', '1234567890', 4, 1, 1, 101, 201, 301, '123 Elm Street', 1, 1, '2024-10-01 09:00:00', '2024-11-01 12:00:00', NULL, NULL, NULL),
    ('Bob Smith', '$2a$12$DV79Jsq6.IK6oIHFPvDapOYIj66IZe6S0Ku24fZ52NE/DCyUP/FMS', 'bob.smith@example.com', '0987654321', 3, 1, 2, 102, 202, 302, '456 Oak Avenue', 2, 1, '2024-10-05 10:00:00', '2024-11-02 15:30:00', NULL, NULL, NULL);
