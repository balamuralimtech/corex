CREATE TABLE Roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE RolePrivileges (
    id INT AUTO_INCREMENT PRIMARY KEY,
    role_id INT NOT NULL,
    module_id INT NOT NULL,
    submodule_id INT NOT NULL,
    privilege_id INT NOT NULL,
    is_selected BOOLEAN DEFAULT FALSE,  -- Boolean column to store if the role is selected
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key to the Role table
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES Roles(id) ON DELETE CASCADE
);

-- Step 1: Insert into Roles table
INSERT INTO Roles (role_name, created_at, updated_at) 
VALUES ('Admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Step 2: Retrieve the ID of the newly inserted Role
SET @role_id = LAST_INSERT_ID();

-- Step 3: Insert into RolePrivileges table using the role_id
INSERT INTO RolePrivileges (role_id, module_id, submodule_id, privilege_id, created_at, updated_at) 
VALUES (@role_id, 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (@role_id, 1, 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);