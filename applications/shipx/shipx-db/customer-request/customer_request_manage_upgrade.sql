ALTER TABLE `shipx_customer_request`
  ADD COLUMN `created_by_user_id` int DEFAULT NULL AFTER `status`,
  ADD COLUMN `created_by_user_name` varchar(100) DEFAULT NULL AFTER `created_by_user_id`;
