CREATE TABLE licenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    organization_id INT NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    remarks VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_license_organization FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT chk_license_date_range CHECK (end_date >= start_date)
);

INSERT INTO licenses (organization_id, start_date, end_date, remarks) VALUES
(1, '2026-01-01', '2050-12-31', 'Annual organization license for organization 1');
