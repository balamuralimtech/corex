DROP TABLE IF EXISTS `shipx_customer_request`;

CREATE TABLE `shipx_customer_request` (
  `id` int NOT NULL AUTO_INCREMENT,
  `request_reference` varchar(40) NOT NULL,
  `customer_type` varchar(20) NOT NULL,
  `customer_name` varchar(255) NOT NULL,
  `origin_country_id` mediumint unsigned NOT NULL,
  `destination_country_id` mediumint unsigned NOT NULL,
  `final_destination_details` varchar(500) DEFAULT NULL,
  `capacity_type` varchar(20) NOT NULL,
  `space_size` varchar(120) DEFAULT NULL,
  `container_count` int DEFAULT NULL,
  `weight_value` decimal(12,3) DEFAULT NULL,
  `weight_unit` varchar(10) DEFAULT NULL,
  `estimated_shipping_date` date NOT NULL,
  `contact_person` varchar(255) NOT NULL,
  `contact_number` varchar(60) NOT NULL,
  `how_you_know_us` varchar(255) DEFAULT NULL,
  `status` varchar(30) NOT NULL DEFAULT 'NEW',
  `created_by_user_id` int DEFAULT NULL,
  `created_by_user_name` varchar(100) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `flag` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shipx_customer_request_reference` (`request_reference`),
  KEY `idx_shipx_customer_request_origin_country` (`origin_country_id`),
  KEY `idx_shipx_customer_request_destination_country` (`destination_country_id`),
  CONSTRAINT `fk_shipx_customer_request_origin_country`
    FOREIGN KEY (`origin_country_id`) REFERENCES `countries` (`id`),
  CONSTRAINT `fk_shipx_customer_request_destination_country`
    FOREIGN KEY (`destination_country_id`) REFERENCES `countries` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
