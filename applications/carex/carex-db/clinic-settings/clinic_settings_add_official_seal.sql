ALTER TABLE `ClinicSettings`
    ADD COLUMN `seal_image` LONGBLOB NULL AFTER `notes`,
    ADD COLUMN `seal_image_mime_type` VARCHAR(100) NULL AFTER `seal_image`,
    ADD COLUMN `show_official_seal` TINYINT(1) NOT NULL DEFAULT 1 AFTER `seal_image_mime_type`,
    ADD COLUMN `seal_display_mode` VARCHAR(20) NULL DEFAULT 'Header' AFTER `show_official_seal`,
    ADD COLUMN `seal_size_px` INT NOT NULL DEFAULT 96 AFTER `seal_display_mode`;
