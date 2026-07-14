SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'organizations'
      AND column_name = 'referral_code'
);

SET @sql = IF(
    @column_exists = 0,
    'ALTER TABLE organizations ADD COLUMN referral_code VARCHAR(150) NULL AFTER website',
    'SELECT ''referral_code column already exists'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
