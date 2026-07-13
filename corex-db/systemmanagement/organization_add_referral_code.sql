ALTER TABLE Organizations
    ADD COLUMN IF NOT EXISTS referral_code VARCHAR(150) NULL AFTER website;
