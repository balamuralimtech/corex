CREATE TABLE branches (
    id INT AUTO_INCREMENT PRIMARY KEY,               -- Unique identifier for each branch
    branch_name VARCHAR(255) NOT NULL,               -- Name of the branch
    organization_id INT NOT NULL,                    -- Foreign key referencing the organization
    country_id MEDIUMINT UNSIGNED NOT NULL,          -- Foreign key referencing the country in the countries table
    address_line_1 VARCHAR(255),                     -- Address of the branch
    address_line_2 VARCHAR(255),                     -- Additional address information (optional)
    state VARCHAR(100),                              -- State or region of the branch
    city VARCHAR(100),                               -- City where the branch is located
    postal_code VARCHAR(20),                         -- Postal code or ZIP code
    phone_number VARCHAR(50),                        -- Contact phone number for the branch
    email VARCHAR(255),                              -- Contact email for the branch
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- When the record was created
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last updated time
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE ON UPDATE CASCADE, -- Foreign key linking to organizations
    FOREIGN KEY (country_id) REFERENCES countries(id) ON DELETE CASCADE ON UPDATE CASCADE -- Foreign key linking to countries
);

INSERT INTO branches (branch_name, organization_id, country_id, address_line_1, address_line_2, state, city, postal_code, phone_number, email)
VALUES 
('Branch 1', 1, 1, '123 First St', NULL, 'State 1', 'City 1', '11111', '123-456-7890', 'branch1@example.com'),
('Branch 2', 2, 1, '456 Second St', NULL, 'State 2', 'City 2', '22222', '123-456-7891', 'branch2@example.com'),
('Branch 3', 3, 2, '789 Third St', NULL, 'State 3', 'City 3', '33333', '123-456-7892', 'branch3@example.com'),
('Branch 4', 1, 3, '101 First Ave', NULL, 'State 4', 'City 4', '44444', '123-456-7893', 'branch4@example.com'),
('Branch 5', 2, 2, '202 Second Ave', NULL, 'State 5', 'City 5', '55555', '123-456-7894', 'branch5@example.com'),
('Branch 6', 3, 1, '303 Third Ave', NULL, 'State 6', 'City 6', '66666', '123-456-7895', 'branch6@example.com'),
('Branch 7', 1, 3, '404 First Blvd', NULL, 'State 7', 'City 7', '77777', '123-456-7896', 'branch7@example.com'),
('Branch 8', 2, 1, '505 Second Blvd', NULL, 'State 8', 'City 8', '88888', '123-456-7897', 'branch8@example.com'),
('Branch 9', 3, 2, '606 Third Blvd', NULL, 'State 9', 'City 9', '99999', '123-456-7898', 'branch9@example.com'),
('Branch 10', 1, 3, '707 First Ln', NULL, 'State 10', 'City 10', '10101', '123-456-7899', 'branch10@example.com');
