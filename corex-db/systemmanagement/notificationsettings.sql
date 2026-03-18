CREATE TABLE NotificationSettings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    organization_id INT NOT NULL,
    email_Id VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    smtpAuth BOOLEAN NOT NULL,
    smtpStarttlsEnable BOOLEAN NOT NULL,
    smtpHost VARCHAR(255) NOT NULL,
    smtpPort VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES Organizations(id)
);

INSERT INTO NotificationSettings (organization_id, email_Id, password, smtpAuth, smtpStarttlsEnable, smtpHost, smtpPort)
VALUES (1, 'admin1@example.com', 'password123', TRUE, TRUE, 'smtp.example.com', '587');

INSERT INTO NotificationSettings (organization_id, email_Id, password, smtpAuth, smtpStarttlsEnable, smtpHost, smtpPort)
VALUES (2, 'admin2@example.com', 'pass456', TRUE, FALSE, 'smtp.mailtrap.io', '465');

INSERT INTO NotificationSettings (organization_id, email_Id, password, smtpAuth, smtpStarttlsEnable, smtpHost, smtpPort)
VALUES (1, 'user1@domain.com', 'secretpass789', TRUE, TRUE, 'smtp.google.com', '587');

INSERT INTO NotificationSettings (organization_id, email_Id, password, smtpAuth, smtpStarttlsEnable, smtpHost, smtpPort)
VALUES (3, 'noreply@domain.com', 'mypassword', FALSE, TRUE, 'smtp.office365.com', '587');

INSERT INTO NotificationSettings (organization_id, email_Id, password, smtpAuth, smtpStarttlsEnable, smtpHost, smtpPort)
VALUES (4, 'support@company.com', 'supportPass', TRUE, TRUE, 'smtp.yahoo.com', '465');

INSERT INTO NotificationSettings (organization_id, email_Id, password, smtpAuth, smtpStarttlsEnable, smtpHost, smtpPort)
VALUES (5, 'alerts@domain.com', 'alertPass123', TRUE, FALSE, 'smtp.sendgrid.com', '2525');

INSERT INTO NotificationSettings (organization_id, email_Id, password, smtpAuth, smtpStarttlsEnable, smtpHost, smtpPort)
VALUES (1, 'info@service.com', 'infoPass456', FALSE, TRUE, 'smtp.fastmail.com', '993');

INSERT INTO NotificationSettings (organization_id, email_Id, password, smtpAuth, smtpStarttlsEnable, smtpHost, smtpPort)
VALUES (3, 'admin@mycompany.com', 'superSecret123', TRUE, TRUE, 'smtp.domainhost.com', '587');

INSERT INTO NotificationSettings (organization_id, email_Id, password, smtpAuth, smtpStarttlsEnable, smtpHost, smtpPort)
VALUES (2, 'no-reply@anotherdomain.com', 'passwordXYZ', TRUE, TRUE, 'smtp.mandrillapp.com', '587');

INSERT INTO NotificationSettings (organization_id, email_Id, password, smtpAuth, smtpStarttlsEnable, smtpHost, smtpPort)
VALUES (4, 'contact@newdomain.com', 'newdomain123', FALSE, TRUE, 'smtp.amazon.com', '465');

