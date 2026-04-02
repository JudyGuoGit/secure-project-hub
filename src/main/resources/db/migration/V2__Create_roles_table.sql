-- V2__Create_roles_table.sql
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_roles_name ON roles(name);

-- Insert default roles
INSERT INTO roles (name, description, created_at, updated_at) VALUES
    ('ADMIN', 'Administrator with full access', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('USER', 'Regular user with standard permissions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('MODERATOR', 'Moderator with elevated permissions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('GUEST', 'Guest user with limited read-only permissions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
