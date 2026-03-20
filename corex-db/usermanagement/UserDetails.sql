CREATE TABLE UserDetails (
    user_id INT PRIMARY KEY AUTO_INCREMENT,            -- Unique ID for each user
    user_name VARCHAR(100) NOT NULL,                   -- User's name
    password VARCHAR(255) NOT NULL,                    -- Encrypted password
    email_id VARCHAR(255) UNIQUE NOT NULL,             -- Email address
    contact VARCHAR(15),                               -- Contact number
    role_id INT NOT NULL,                              -- Role foreign key
    organization_id INT,                               -- Organization foreign key
    branch_id INT,                                     -- Branch foreign key
    country_id MEDIUMINT UNSIGNED,                     -- Country foreign key (matching mediumint unsigned)
    state_id MEDIUMINT UNSIGNED,                                      -- State foreign key
    city_id MEDIUMINT UNSIGNED,                                       -- City foreign key
    address TEXT,                                      -- Full address
    access_right_id INT NOT NULL,                      -- Foreign key for access_right
    status_id INT NOT NULL,                            -- Foreign key for status
    last_password_change TIMESTAMP DEFAULT NULL,       -- Timestamp for last password change
    last_successful_login TIMESTAMP DEFAULT NULL,	   -- Timestamp for last successful login
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,    -- Timestamp when the user is created
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  -- Timestamp when the user is updated

    -- Foreign key constraints
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES Roles(id),
    CONSTRAINT fk_user_organization FOREIGN KEY (organization_id) REFERENCES Organizations(id),
    CONSTRAINT fk_user_branch FOREIGN KEY (branch_id) REFERENCES Branches(id),
    CONSTRAINT fk_user_country FOREIGN KEY (country_id) REFERENCES Countries(id),
    CONSTRAINT fk_user_state FOREIGN KEY (state_id) REFERENCES States(id),
    CONSTRAINT fk_user_city FOREIGN KEY (city_id) REFERENCES Cities(id)
);

INSERT INTO UserDetails (user_name, password, email_id, contact, role_id, organization_id, branch_id, country_id, state_id, city_id, address, access_right_id, status_id, last_password_change, last_successful_login)
VALUES
    ('admin', 'admin123', 'alice.johnson@example.com', '1234567890', 1, 1, 1, 101, 201, 301, '123 Elm Street', 1, 1, '2024-10-01 09:00:00', '2024-11-01 12:00:00'),
    ('Bob Smith', 'encryptedpassword2', 'bob.smith@example.com', '0987654321', 1, 1, 2, 102, 202, 302, '456 Oak Avenue', 2, 1, '2024-10-05 10:00:00', '2024-11-02 15:30:00'),
    ('Cathy Lee', 'encryptedpassword3', 'cathy.lee@example.com', '1122334455', 1, 2, 3, 103, 203, 303, '789 Maple Blvd', 1, 1, '2024-10-10 11:00:00', '2024-11-03 09:15:00');