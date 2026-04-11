CREATE TABLE IF NOT EXISTS demo_request (
    id INT NOT NULL AUTO_INCREMENT,
    clinic_name VARCHAR(150) NOT NULL,
    work_email VARCHAR(150) NOT NULL,
    notes VARCHAR(1000) NULL,
    demo_done TINYINT(1) NOT NULL DEFAULT 0,
    demo_done_at TIMESTAMP NULL DEFAULT NULL,
    demo_done_by VARCHAR(100) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_demo_request_created_at (created_at),
    KEY idx_demo_request_work_email (work_email),
    KEY idx_demo_request_demo_done (demo_done)
);

CREATE INDEX idx_demo_request_demo_done ON demo_request (demo_done);
