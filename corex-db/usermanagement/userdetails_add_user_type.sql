ALTER TABLE UserDetails
    ADD COLUMN user_type VARCHAR(32) NOT NULL DEFAULT 'GENERAL_USER' AFTER email_id;

UPDATE UserDetails
SET user_type = 'GENERAL_USER'
WHERE user_type IS NULL OR TRIM(user_type) = '';
