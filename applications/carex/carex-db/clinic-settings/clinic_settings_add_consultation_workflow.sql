ALTER TABLE `ClinicSettings`
    ADD COLUMN `reception_token_workflow_enabled` TINYINT(1) NOT NULL DEFAULT 1
    AFTER `require_medical_certificate`;
