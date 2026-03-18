CREATE TABLE designations (
    id INT AUTO_INCREMENT PRIMARY KEY,               -- Unique identifier for each designation
    organization_id INT NOT NULL,                    -- Foreign key referencing the organization
    designation_name VARCHAR(255) NOT NULL,          -- Name of the designation
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,   -- When the record was created
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last updated time
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE ON UPDATE CASCADE  -- Foreign key linking to the organization table
);


INSERT INTO designations (organization_id, designation_name)
VALUES
    (1, 'HR Manager'),
    (1, 'HR Executive'),
    (1, 'Finance Manager'),
    (1, 'Accountant'),
    (2, 'Marketing Director'),
    (2, 'SEO Specialist'),
    (2, 'Sales Manager'),
    (3, 'R&D Engineer'),
    (3, 'Product Manager'),
    (4, 'Customer Service Representative');
