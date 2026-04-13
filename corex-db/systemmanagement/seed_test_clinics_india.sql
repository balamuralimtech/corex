-- Seeds 500 synthetic India clinic organizations and 500 linked branches.
-- Safe assumptions:
-- 1. `countries` is populated from world.sql and contains India (`iso2 = 'IN'`)
-- 2. `Organizations` and `Branches` tables already exist
-- 3. This script is for non-production/demo/performance data only

SET @india_country_id := (
    SELECT id
    FROM countries
    WHERE iso2 = 'IN'
    ORDER BY id
    LIMIT 1
);

-- Stop early if India is unavailable in the reference data.
SELECT CASE
           WHEN @india_country_id IS NULL THEN 'India country row not found in countries table'
           ELSE CONCAT('India country id resolved as ', @india_country_id)
       END AS seed_status;

DROP TEMPORARY TABLE IF EXISTS tmp_india_clinic_locations;
CREATE TEMPORARY TABLE tmp_india_clinic_locations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    state_name VARCHAR(100) NOT NULL,
    city_name VARCHAR(100) NOT NULL,
    postal_prefix VARCHAR(10) NOT NULL
);

INSERT INTO tmp_india_clinic_locations (state_name, city_name, postal_prefix) VALUES
('Tamil Nadu', 'Chennai', '600'),
('Tamil Nadu', 'Coimbatore', '641'),
('Tamil Nadu', 'Madurai', '625'),
('Tamil Nadu', 'Tiruchirappalli', '620'),
('Tamil Nadu', 'Salem', '636'),
('Tamil Nadu', 'Tirunelveli', '627'),
('Tamil Nadu', 'Erode', '638'),
('Karnataka', 'Bengaluru', '560'),
('Karnataka', 'Mysuru', '570'),
('Karnataka', 'Mangaluru', '575'),
('Karnataka', 'Hubballi', '580'),
('Karnataka', 'Belagavi', '590'),
('Kerala', 'Thiruvananthapuram', '695'),
('Kerala', 'Kochi', '682'),
('Kerala', 'Kozhikode', '673'),
('Kerala', 'Thrissur', '680'),
('Kerala', 'Kannur', '670'),
('Maharashtra', 'Mumbai', '400'),
('Maharashtra', 'Pune', '411'),
('Maharashtra', 'Nagpur', '440'),
('Maharashtra', 'Nashik', '422'),
('Maharashtra', 'Aurangabad', '431'),
('Telangana', 'Hyderabad', '500'),
('Telangana', 'Warangal', '506'),
('Telangana', 'Nizamabad', '503'),
('Andhra Pradesh', 'Visakhapatnam', '530'),
('Andhra Pradesh', 'Vijayawada', '520'),
('Andhra Pradesh', 'Guntur', '522'),
('Andhra Pradesh', 'Tirupati', '517'),
('Delhi', 'New Delhi', '110'),
('West Bengal', 'Kolkata', '700'),
('West Bengal', 'Siliguri', '734'),
('West Bengal', 'Durgapur', '713'),
('Gujarat', 'Ahmedabad', '380'),
('Gujarat', 'Surat', '395'),
('Gujarat', 'Vadodara', '390'),
('Gujarat', 'Rajkot', '360'),
('Rajasthan', 'Jaipur', '302'),
('Rajasthan', 'Jodhpur', '342'),
('Rajasthan', 'Udaipur', '313'),
('Madhya Pradesh', 'Indore', '452'),
('Madhya Pradesh', 'Bhopal', '462'),
('Madhya Pradesh', 'Jabalpur', '482'),
('Uttar Pradesh', 'Lucknow', '226'),
('Uttar Pradesh', 'Kanpur', '208'),
('Uttar Pradesh', 'Noida', '201'),
('Uttar Pradesh', 'Varanasi', '221'),
('Bihar', 'Patna', '800'),
('Odisha', 'Bhubaneswar', '751'),
('Punjab', 'Ludhiana', '141'),
('Haryana', 'Gurugram', '122'),
('Assam', 'Guwahati', '781');

DROP PROCEDURE IF EXISTS seed_test_clinics_india;
DELIMITER $$
CREATE PROCEDURE seed_test_clinics_india()
BEGIN
    DECLARE v_counter INT DEFAULT 1;
    DECLARE v_location_count INT DEFAULT 0;
    DECLARE v_org_location_id INT;
    DECLARE v_branch_location_id INT;
    DECLARE v_org_state VARCHAR(100);
    DECLARE v_org_city VARCHAR(100);
    DECLARE v_org_postal_prefix VARCHAR(10);
    DECLARE v_branch_state VARCHAR(100);
    DECLARE v_branch_city VARCHAR(100);
    DECLARE v_branch_postal_prefix VARCHAR(10);
    DECLARE v_org_id INT;
    DECLARE v_now TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

    IF @india_country_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot seed clinics because India is missing in countries table';
    END IF;

    SELECT COUNT(*) INTO v_location_count FROM tmp_india_clinic_locations;

    WHILE v_counter <= 500 DO
        SET v_org_location_id = ((v_counter - 1) MOD v_location_count) + 1;
        SET v_branch_location_id = ((v_counter + 16) MOD v_location_count) + 1;

        SELECT state_name, city_name, postal_prefix
        INTO v_org_state, v_org_city, v_org_postal_prefix
        FROM tmp_india_clinic_locations
        WHERE id = v_org_location_id;

        SELECT state_name, city_name, postal_prefix
        INTO v_branch_state, v_branch_city, v_branch_postal_prefix
        FROM tmp_india_clinic_locations
        WHERE id = v_branch_location_id;

        INSERT INTO Organizations (
            organization_name,
            country_id,
            state,
            address_line_1,
            address_line_2,
            city,
            postal_code,
            phone_number,
            email,
            website,
            referral_code,
            image,
            created_at,
            updated_at
        ) VALUES (
            CONCAT(
                ELT(((v_counter - 1) MOD 10) + 1,
                    'CarePlus', 'Aarogya', 'MediSpring', 'LifeBridge', 'HealNest',
                    'NovaCare', 'PulsePoint', 'WellPath', 'Cliniva', 'VitalCrest'
                ),
                ' Clinic ',
                LPAD(v_counter, 3, '0')
            ),
            @india_country_id,
            v_org_state,
            CONCAT(100 + (v_counter MOD 700), ', ', ELT(((v_counter - 1) MOD 8) + 1, 'Lake View Road', 'MG Road', 'Temple Street', 'Station Road', 'Gandhi Nagar', 'Anna Salai', 'Ring Road', 'Health Avenue')),
            CONCAT('Near ', ELT(((v_counter - 1) MOD 8) + 1, 'Metro Station', 'Bus Stand', 'City Hospital', 'Market Circle', 'Collector Office', 'Tech Park', 'Railway Junction', 'Medical College')),
            v_org_city,
            CONCAT(v_org_postal_prefix, LPAD((v_counter MOD 900) + 100, 3, '0')),
            CONCAT('+91-9', LPAD(100000000 + v_counter, 9, '0')),
            CONCAT('clinic', LPAD(v_counter, 3, '0'), '@carextest.in'),
            CONCAT('https://clinic', LPAD(v_counter, 3, '0'), '.carextest.in'),
            NULL,
            NULL,
            v_now,
            v_now
        );

        SET v_org_id = LAST_INSERT_ID();

        INSERT INTO Branches (
            branch_name,
            organization_id,
            country_id,
            address_line_1,
            address_line_2,
            state,
            city,
            postal_code,
            phone_number,
            email,
            created_at,
            updated_at
        ) VALUES (
            CONCAT('Main Branch ', LPAD(v_counter, 3, '0')),
            v_org_id,
            @india_country_id,
            CONCAT(10 + (v_counter MOD 500), ', ', ELT(((v_counter + 2) MOD 8) + 1, 'Ring Road', 'Hospital Road', 'Nehru Street', 'Residency Road', 'Central Avenue', 'Link Road', 'Garden Street', 'Park Lane')),
            CONCAT('Opp. ', ELT(((v_counter + 1) MOD 8) + 1, 'Apollo Pharmacy', 'Community Hall', 'Old Bus Depot', 'District Court', 'Mall Junction', 'River Bridge', 'Police Quarters', 'Sports Complex')),
            v_branch_state,
            v_branch_city,
            CONCAT(v_branch_postal_prefix, LPAD((v_counter MOD 850) + 120, 3, '0')),
            CONCAT('+91-8', LPAD(200000000 + v_counter, 9, '0')),
            CONCAT('branch', LPAD(v_counter, 3, '0'), '@carextest.in'),
            v_now,
            v_now
        );

        SET v_counter = v_counter + 1;
    END WHILE;
END $$
DELIMITER ;

CALL seed_test_clinics_india();

DROP PROCEDURE IF EXISTS seed_test_clinics_india;
DROP TEMPORARY TABLE IF EXISTS tmp_india_clinic_locations;

SELECT
    COUNT(*) AS seeded_clinic_count
FROM Organizations
WHERE email LIKE '%@carextest.in';

SELECT
    COUNT(*) AS seeded_branch_count
FROM Branches
WHERE email LIKE 'branch%@carextest.in';
