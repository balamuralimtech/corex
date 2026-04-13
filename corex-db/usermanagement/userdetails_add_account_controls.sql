ALTER TABLE UserDetails
    ADD COLUMN account_disabled BOOLEAN NOT NULL DEFAULT FALSE AFTER status_id,
    ADD COLUMN account_locked BOOLEAN NOT NULL DEFAULT FALSE AFTER account_disabled;
