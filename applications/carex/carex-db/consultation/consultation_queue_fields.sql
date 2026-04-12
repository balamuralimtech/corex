ALTER TABLE Consultations
    ADD COLUMN token_number INT NULL AFTER consultation_date,
    ADD COLUMN patient_age_years INT NULL AFTER token_number,
    ADD COLUMN temperature_celsius VARCHAR(20) NULL AFTER patient_age_years,
    ADD COLUMN weight_kg VARCHAR(20) NULL AFTER temperature_celsius,
    ADD COLUMN blood_pressure VARCHAR(30) NULL AFTER weight_kg;

ALTER TABLE Consultations
    ADD INDEX idx_consultations_token (token_number);
