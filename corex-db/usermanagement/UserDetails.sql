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
    ('Cathy Lee', 'encryptedpassword3', 'cathy.lee@example.com', '1122334455', 1, 2, 3, 103, 203, 303, '789 Maple Blvd', 1, 1, '2024-10-10 11:00:00', '2024-11-03 09:15:00'),
    ('David Brown', 'encryptedpassword4', 'david.brown@example.com', '2233445566', 1, 2, 1, 104, 204, 304, '101 Pine Road', 3, 2, '2024-10-15 12:00:00', '2024-11-04 14:45:00'),
    ('Emma Green', 'encryptedpassword5', 'emma.green@example.com', '3344556677', 1, 1, 2, 105, 205, 305, '202 Birch Lane', 2, 1, '2024-10-20 13:00:00', '2024-11-05 16:30:00'),
    ('Frank White', 'encryptedpassword6', 'frank.white@example.com', '4455667788', 1, 3, 1, 106, 206, 306, '303 Cedar Court', 3, 2, '2024-10-25 14:00:00', '2024-11-06 08:00:00'),
    ('Grace Kim', 'encryptedpassword7', 'grace.kim@example.com', '5566778899', 1, 3, 2, 107, 207, 307, '404 Spruce Circle', 1, 1, '2024-10-30 15:00:00', '2024-11-07 12:00:00'),
    ('Henry Adams', 'encryptedpassword8', 'henry.adams@example.com', '6677889900', 1, 4, 3, 108, 208, 308, '505 Ash Street', 2, 2, '2024-10-01 16:00:00', '2024-11-08 11:30:00'),
    ('Ivy Wong', 'encryptedpassword9', 'ivy.wong@example.com', '7788990011', 1, 4, 1, 109, 209, 309, '606 Willow Ave', 3, 1, '2024-10-02 17:00:00', '2024-11-09 10:15:00'),
    ('Jack Miller', 'encryptedpassword10', 'jack.miller@example.com', '8899001122', 1, 5, 2, 110, 210, 310, '707 Poplar Way', 1, 2, '2024-10-03 18:00:00', '2024-11-10 13:45:00');