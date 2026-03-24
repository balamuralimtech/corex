DROP TABLE IF EXISTS applicationtheme;

CREATE TABLE applicationtheme (
    id INT PRIMARY KEY AUTO_INCREMENT,
    userid INT NOT NULL,
    theme VARCHAR(50) DEFAULT NULL,
    layout VARCHAR(50) DEFAULT NULL,
    menuclass VARCHAR(50) DEFAULT NULL,
    profilemode VARCHAR(50) DEFAULT NULL,
    menulayout VARCHAR(50) DEFAULT NULL,
    inputstyle VARCHAR(50) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_applicationtheme_user FOREIGN KEY (userid) REFERENCES UserDetails(user_id),
    CONSTRAINT uk_applicationtheme_user UNIQUE (userid)
);
