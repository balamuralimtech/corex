ALTER TABLE demo_request
    ADD COLUMN demo_done TINYINT(1) NOT NULL DEFAULT 0 AFTER notes,
    ADD COLUMN demo_done_at TIMESTAMP NULL DEFAULT NULL AFTER demo_done,
    ADD COLUMN demo_done_by VARCHAR(100) NULL AFTER demo_done_at;

CREATE INDEX idx_demo_request_demo_done ON demo_request (demo_done);
