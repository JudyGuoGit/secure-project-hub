-- V3__Create_permissions_table.sql
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    UNIQUE(resource, action)
);

CREATE INDEX idx_permissions_name ON permissions(name);
CREATE INDEX idx_permissions_resource ON permissions(resource);

-- Insert default permissions
INSERT INTO permissions (name, description, resource, action, created_at, updated_at) VALUES
    -- User permissions
    ('USER_READ', 'Read user information', 'USER', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('USER_CREATE', 'Create new users', 'USER', 'CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('USER_UPDATE', 'Update user information', 'USER', 'UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('USER_DELETE', 'Delete users', 'USER', 'DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    -- Role permissions
    ('ROLE_READ', 'Read role information', 'ROLE', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROLE_CREATE', 'Create new roles', 'ROLE', 'CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROLE_UPDATE', 'Update role information', 'ROLE', 'UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROLE_DELETE', 'Delete roles', 'ROLE', 'DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    -- Permission permissions
    ('PERMISSION_READ', 'Read permission information', 'PERMISSION', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PERMISSION_CREATE', 'Create new permissions', 'PERMISSION', 'CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PERMISSION_UPDATE', 'Update permission information', 'PERMISSION', 'UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PERMISSION_DELETE', 'Delete permissions', 'PERMISSION', 'DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    -- Audit permissions
    ('AUDIT_READ', 'Read audit logs', 'AUDIT', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('AUDIT_DELETE', 'Delete audit logs', 'AUDIT', 'DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
