CREATE TABLE departments (
    id INT AUTO_INCREMENT PRIMARY KEY,               -- Unique identifier for each department
    organization_id INT NOT NULL,                    -- Foreign key referencing the organization
    department_name VARCHAR(255) NOT NULL,           -- Name of the department
    phone_number VARCHAR(50),                         -- Contact phone number for the department
    email VARCHAR(255),                               -- Contact email for the department
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,   -- When the record was created
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last updated time
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE ON UPDATE CASCADE  -- Foreign key linking to the organization table
);

INSERT INTO departments (organization_id, department_name, phone_number, email)
VALUES
    (1, 'Human Resources', '(555) 101-2345', 'hr@example.com'),
    (1, 'Finance', '(555) 101-2346', 'finance@example.com'),
    (2, 'Marketing', '(555) 202-3456', 'marketing@example.com'),
    (2, 'Sales', '(555) 202-3457', 'sales@example.com'),
    (3, 'Research and Development', '(555) 303-4567', 'rnd@example.com'),
    (3, 'IT Support', '(555) 303-4568', 'itsupport@example.com'),
    (4, 'Customer Service', '(555) 404-5678', 'customerservice@example.com'),
    (5, 'Logistics', '(555) 505-6789', 'logistics@example.com'),
    (5, 'Legal', '(555) 505-6790', 'legal@example.com'),
    (6, 'Public Relations', '(555) 606-7891', 'pr@example.com');
