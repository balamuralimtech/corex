CREATE TABLE IF NOT EXISTS referrer_profile (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NULL,
    referrer_name VARCHAR(150) NOT NULL,
    referrer_category VARCHAR(40) NOT NULL,
    referral_code VARCHAR(40) NOT NULL,
    commission_percentage DECIMAL(8,2) NULL,
    is_active BIT NOT NULL DEFAULT b'1',
    notes VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_referrer_profile_code (referral_code),
    UNIQUE KEY uk_referrer_profile_user (user_id),
    CONSTRAINT fk_referrer_profile_user FOREIGN KEY (user_id) REFERENCES UserDetails (user_id)
);

CREATE TABLE IF NOT EXISTS organization_referral_profile (
    id INT NOT NULL AUTO_INCREMENT,
    organization_id INT NOT NULL,
    referral_code VARCHAR(40) NOT NULL,
    is_active BIT NOT NULL DEFAULT b'1',
    notes VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_org_referral_profile_code (referral_code),
    UNIQUE KEY uk_org_referral_profile_org (organization_id),
    CONSTRAINT fk_org_referral_profile_org FOREIGN KEY (organization_id) REFERENCES Organizations (id)
);

CREATE TABLE IF NOT EXISTS referral_attribution (
    id INT NOT NULL AUTO_INCREMENT,
    referrer_profile_id INT NULL,
    organization_referral_profile_id INT NULL,
    referred_organization_id INT NOT NULL,
    referral_code VARCHAR(40) NOT NULL,
    referral_source_type VARCHAR(40) NOT NULL,
    benefit_type VARCHAR(40) NOT NULL,
    plan_code VARCHAR(40) NOT NULL,
    subscription_amount DECIMAL(12,2) NOT NULL,
    free_months_awarded INT NOT NULL DEFAULT 0,
    payment_gateway_code VARCHAR(40) NULL,
    payment_order_id VARCHAR(120) NULL,
    payment_id VARCHAR(120) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_referral_attr_referrer (referrer_profile_id),
    KEY idx_referral_attr_org_referrer (organization_referral_profile_id),
    KEY idx_referral_attr_referred_org (referred_organization_id),
    KEY idx_referral_attr_code (referral_code),
    CONSTRAINT fk_referral_attr_referrer FOREIGN KEY (referrer_profile_id) REFERENCES referrer_profile (id),
    CONSTRAINT fk_referral_attr_org_referrer FOREIGN KEY (organization_referral_profile_id) REFERENCES organization_referral_profile (id),
    CONSTRAINT fk_referral_attr_referred_org FOREIGN KEY (referred_organization_id) REFERENCES Organizations (id)
);

CREATE TABLE IF NOT EXISTS referral_commission (
    id INT NOT NULL AUTO_INCREMENT,
    referral_attribution_id INT NOT NULL,
    referrer_profile_id INT NOT NULL,
    commission_amount DECIMAL(12,2) NOT NULL,
    commission_status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP NULL,
    PRIMARY KEY (id),
    KEY idx_referral_commission_attr (referral_attribution_id),
    KEY idx_referral_commission_referrer (referrer_profile_id),
    CONSTRAINT fk_referral_commission_attr FOREIGN KEY (referral_attribution_id) REFERENCES referral_attribution (id),
    CONSTRAINT fk_referral_commission_referrer FOREIGN KEY (referrer_profile_id) REFERENCES referrer_profile (id)
);
