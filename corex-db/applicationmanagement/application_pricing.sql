CREATE TABLE IF NOT EXISTS application_pricing (
    id INT NOT NULL AUTO_INCREMENT,
    application_code VARCHAR(80) NOT NULL,
    application_name VARCHAR(120) NOT NULL,
    country_code VARCHAR(10) NOT NULL,
    country_name VARCHAR(120) NOT NULL,
    currency_code VARCHAR(10) NOT NULL,
    currency_symbol VARCHAR(10) NOT NULL,
    one_month_price DECIMAL(12,2) NOT NULL,
    six_month_price DECIMAL(12,2) NOT NULL,
    one_year_price DECIMAL(12,2) NOT NULL,
    display_order INT NULL,
    is_active BIT NOT NULL DEFAULT b'1',
    notes VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_application_pricing_app_country (application_code, country_code)
);
