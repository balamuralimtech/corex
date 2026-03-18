CREATE TABLE bankdetails (
    id INT AUTO_INCREMENT PRIMARY KEY, -- Unique identifier for each record
    organization_id INT NOT NULL, -- Foreign key referencing the organization
    bank_account_details TEXT NOT NULL, -- Terms and conditions note
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- Timestamp for creation
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL, -- Timestamp for last update
    CONSTRAINT fk_organization_bankdetails FOREIGN KEY (organization_id) REFERENCES organizations(id) -- Foreign key constraint
);

INSERT INTO bankdetails (organization_id, bank_account_details)
VALUES
    (1, 'Bank account details for organization 1'),
    (2, 'Bank account details for organization 2'),
    (3, 'Bank account details for organization 3'),
    (4, 'Bank account details for organization 4'),
    (5, 'Bank account details for organization 5'),
    (6, 'Bank account details for organization 6'),
    (7, 'Bank account details for organization 7'),
    (8, 'Bank account details for organization 8'),
    (9, 'Bank account details for organization 9'),
    (10, 'Bank account details for organization 10');