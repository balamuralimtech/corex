-- ============================================================
-- Performance Optimization Indexes for CoreX Application
-- Phase 1: Critical Database Indexes
-- Created: 2026-04-07
-- Purpose: Support 500 organizations with 2,500 concurrent users
-- ============================================================

USE corex;  -- Replace with your actual database name

-- ============================================================
-- 1. UserDetails Table Indexes
-- ============================================================
-- Note: Foreign key columns (organization_id, role_id, branch_id, country_id,
-- state_id, city_id) already have indexes created automatically by MySQL.
-- We only create additional indexes for non-FK columns and composite indexes.

-- Index for status-based queries (active/inactive users)
CREATE INDEX idx_user_status ON UserDetails(status_id);

-- Index for email lookups (email_id has unique constraint but may need index)
CREATE INDEX idx_user_email ON UserDetails(email_id);

-- Index for username lookups
CREATE INDEX idx_user_username ON UserDetails(user_name);

-- Index for session management
CREATE INDEX idx_user_session ON UserDetails(last_session_id);

-- Composite index for dashboard queries (status + organization)
CREATE INDEX idx_user_status_org ON UserDetails(status_id, organization_id);

-- Composite index for session validation
CREATE INDEX idx_user_session_status ON UserDetails(last_session_id, status_id);

-- Index for user type filtering
CREATE INDEX idx_user_type ON UserDetails(user_type);

-- ============================================================
-- 2. Organizations Table Indexes
-- ============================================================
-- Note: country_id is a foreign key and already has an index

-- Index for organization name searches
CREATE INDEX idx_org_name ON Organizations(organization_name);

-- ============================================================
-- 3. Roles Table Indexes
-- ============================================================

-- Index for role name lookups (if role name exists)
-- CREATE INDEX idx_role_name ON Roles(role_name);

-- ============================================================
-- 4. Branches Table Indexes
-- ============================================================
-- Note: organization_id is a foreign key and already has an index

-- ============================================================
-- 5. UserActivities Table Indexes
-- ============================================================

-- Index for user activity lookups
CREATE INDEX idx_activity_user ON UserActivities(user_id);

-- Index for activity date range queries
CREATE INDEX idx_activity_timestamp ON UserActivities(created_at);

-- Composite index for user activity date filtering
CREATE INDEX idx_activity_user_date ON UserActivities(user_id, created_at);

-- ============================================================
-- 6. RolePrivileges Table Indexes
-- ============================================================
-- Note: role_id is a foreign key and already has an index

-- Index for module lookup
CREATE INDEX idx_role_priv_module ON RolePrivileges(module_id);

-- Index for submodule lookup
CREATE INDEX idx_role_priv_submodule ON RolePrivileges(submodule_id);

-- Index for privilege lookup
CREATE INDEX idx_role_priv_privilege ON RolePrivileges(privilege_id);

-- ============================================================
-- 7. application_notification Table Indexes
-- ============================================================

-- Index for notification date queries
CREATE INDEX idx_notif_created_at ON application_notification(created_at);

-- Index for notification user tracking
CREATE INDEX idx_notif_created_by_user ON application_notification(created_by_user_id);

-- ============================================================
-- 8. Licenses Table Indexes
-- ============================================================
-- Note: organization_id is a foreign key and already has an index

-- Index for license start date monitoring
CREATE INDEX idx_license_start_date ON Licenses(start_date);

-- Index for license end date (expiry) monitoring
CREATE INDEX idx_license_end_date ON Licenses(end_date);

-- Composite index for active licenses per organization (date range queries)
CREATE INDEX idx_license_org_dates ON Licenses(organization_id, start_date, end_date);

-- ============================================================
-- Verification Queries
-- ============================================================

-- Run these queries to verify indexes were created successfully

-- Check UserDetails indexes
SHOW INDEX FROM UserDetails;

-- Check Organizations indexes
SHOW INDEX FROM Organizations;

-- Check table statistics
SELECT
    TABLE_NAME,
    TABLE_ROWS,
    AVG_ROW_LENGTH,
    DATA_LENGTH,
    INDEX_LENGTH,
    ROUND(INDEX_LENGTH/DATA_LENGTH, 2) AS INDEX_RATIO
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'coretix_db'
AND TABLE_NAME IN ('UserDetails', 'Organizations', 'Branches', 'Roles', 'UserActivities')
ORDER BY TABLE_ROWS DESC;

-- ============================================================
-- Performance Testing Queries
-- ============================================================

-- Test query performance before and after indexes
-- 1. User list by organization
EXPLAIN SELECT * FROM UserDetails WHERE organization_id = 1;

-- 2. Active users by status
EXPLAIN SELECT * FROM UserDetails WHERE status_id = 1;

-- 3. User login validation
EXPLAIN SELECT * FROM UserDetails WHERE user_name = 'testuser';

-- 4. Session validation
EXPLAIN SELECT * FROM UserDetails WHERE last_session_id = 'session123' AND status_id = 1;

-- ============================================================
-- Maintenance Recommendations
-- ============================================================

-- Analyze tables after index creation (MySQL 8.0+)
ANALYZE TABLE UserDetails, Organizations, Branches, Roles, UserActivities;

-- Update optimizer statistics
OPTIMIZE TABLE UserDetails, Organizations, Branches, Roles, UserActivities;

-- ============================================================
-- Rollback Script (Use with caution - only if needed)
-- ============================================================

/*
-- Uncomment to drop indexes if needed
-- Note: Do NOT drop foreign key indexes as they are required by MySQL

-- UserDetails indexes
DROP INDEX idx_user_status ON UserDetails;
DROP INDEX idx_user_email ON UserDetails;
DROP INDEX idx_user_username ON UserDetails;
DROP INDEX idx_user_session ON UserDetails;
DROP INDEX idx_user_status_org ON UserDetails;
DROP INDEX idx_user_session_status ON UserDetails;
DROP INDEX idx_user_type ON UserDetails;

-- Organizations indexes
DROP INDEX idx_org_name ON Organizations;

-- UserActivities indexes
DROP INDEX idx_activity_user ON UserActivities;
DROP INDEX idx_activity_timestamp ON UserActivities;
DROP INDEX idx_activity_user_date ON UserActivities;

-- RolePrivileges indexes
DROP INDEX idx_role_priv_module ON RolePrivileges;
DROP INDEX idx_role_priv_submodule ON RolePrivileges;
DROP INDEX idx_role_priv_privilege ON RolePrivileges;

-- application_notification indexes
DROP INDEX idx_notif_created_at ON application_notification;
DROP INDEX idx_notif_created_by_user ON application_notification;

-- Licenses indexes
DROP INDEX idx_license_start_date ON Licenses;
DROP INDEX idx_license_end_date ON Licenses;
DROP INDEX idx_license_org_dates ON Licenses;
*/

-- ============================================================
-- Notes:
-- ============================================================
-- 1. Run this script during off-peak hours
-- 2. For large tables (>1M rows), index creation may take time
-- 3. Monitor disk space - indexes require additional storage
-- 4. Expected index overhead: 20-30% of table size
-- 5. After creation, monitor query performance using EXPLAIN
-- ============================================================
