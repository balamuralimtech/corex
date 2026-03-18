CREATE TABLE organizations (
    id INT AUTO_INCREMENT PRIMARY KEY,               -- Unique identifier for each organization
    organization_name VARCHAR(255) NOT NULL,         -- Name of the organization
    country_id MEDIUMINT UNSIGNED NOT NULL,          -- Foreign key referencing the country in the countries table
    address_line_1 VARCHAR(255),                     -- Address of the organization
    address_line_2 VARCHAR(255),                     -- Additional address information (optional)
    state VARCHAR(100),                              -- State or region of the organization
    city VARCHAR(100),                               -- City where the organization is located
    postal_code VARCHAR(20),                         -- Postal code or ZIP code
    phone_number VARCHAR(50),                        -- Contact phone number for the organization
    email VARCHAR(255),                              -- Contact email for the organization
    website VARCHAR(255),                            -- Website of the organization
    image BLOB,                                      -- Binary data for the organization's image
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- When the record was created
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last updated time
    FOREIGN KEY (country_id) REFERENCES countries(id) ON DELETE CASCADE ON UPDATE CASCADE -- Foreign key linking to the countries table
);

INSERT INTO organizations (
    organization_name, country_id, state, address_line_1, address_line_2, 
    city, postal_code, phone_number, email, website, image
)
VALUES 
-- Organization 1
('Tech Innovators Inc.', 1, 'California', '123 Silicon Valley Road', 'Suite 400', 
 'San Francisco', '94107', '+1-555-123456', 'info@techinnovators.com', 'www.techinnovators.com', NULL),

-- Organization 2
('Global Logistics Ltd.', 2, 'New South Wales', '45 Harbour Street', 'Level 10', 
 'Sydney', '2000', '+61-2-5556789', 'contact@globallogistics.com', 'www.globallogistics.com', NULL),

-- Organization 3
('Fintech Solutions', 3, 'London', '12 High Street', '', 
 'London', 'EC1A 4XY', '+44-20-12345678', 'support@fintechsolutions.com', 'www.fintechsolutions.co.uk', NULL),

-- Organization 4
('MediHealth Systems', 4, 'Bavaria', '34 Medical Park', '', 
 'Munich', '80331', '+49-89-1234567', 'info@medihealth.com', 'www.medihealth.com', NULL),

-- Organization 5
('Green Energy Co.', 5, 'Tokyo', '99 Renewable Way', 'Tower 3', 
 'Tokyo', '100-0001', '+81-3-12345678', 'hello@greenenergy.co.jp', 'www.greenenergy.co.jp', NULL),

-- Organization 6
('EduTech Learning', 6, 'Ontario', '78 University Avenue', 'Building 5', 
 'Toronto', 'M5G 2J5', '+1-416-5557890', 'contact@edutechlearning.com', 'www.edutechlearning.com', NULL),

-- Organization 7
('Fashion Trends Group', 7, 'Paris', '11 Rue de la Mode', 'Suite 8A', 
 'Paris', '75001', '+33-1-5551234', 'info@fashiontrends.fr', 'www.fashiontrends.fr', NULL),

-- Organization 8
('Digital Media House', 8, 'Maharashtra', '4 Film City Road', '', 
 'Mumbai', '400001', '+91-22-55567890', 'info@digitalmedia.com', 'www.digitalmedia.com', NULL),

-- Organization 9
('Innovative Manufacturing', 9, 'Shanghai', '22 Innovation Boulevard', 'Tech Park', 
 'Shanghai', '200000', '+86-21-5551234', 'support@innomanufacturing.cn', 'www.innomanufacturing.cn', NULL),

-- Organization 10
('SmartTech Robotics', 10, 'Seoul', '17 Robot Valley', 'Suite 300', 
 'Seoul', '100-100', '+82-2-5551234', 'support@smarttechrobotics.kr', 'www.smarttechrobotics.kr', NULL);