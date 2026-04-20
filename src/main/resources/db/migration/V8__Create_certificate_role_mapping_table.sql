-- Certificate to Role Mapping Table
-- Maps client certificate CNs to override roles (admin escalation)
-- If not found, default role is determined by certificate OU

CREATE TABLE certificate_role_mapping (
    id SERIAL PRIMARY KEY,
    certificate_cn VARCHAR(255) NOT NULL UNIQUE,
    certificate_serial VARCHAR(255),
    override_role_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    notes TEXT,
    
    CONSTRAINT fk_override_role 
        FOREIGN KEY (override_role_id) 
        REFERENCES roles(id) ON DELETE RESTRICT
);

-- Index for fast lookups by certificate CN
CREATE INDEX idx_cert_cn ON certificate_role_mapping(certificate_cn);
CREATE INDEX idx_cert_serial ON certificate_role_mapping(certificate_serial);

-- Add comments
COMMENT ON TABLE certificate_role_mapping IS 'Maps client certificate CNs to roles for privilege escalation. Default role comes from certificate OU.';
COMMENT ON COLUMN certificate_role_mapping.certificate_cn IS 'Common Name (CN) from certificate Subject DN';
COMMENT ON COLUMN certificate_role_mapping.certificate_serial IS 'Optional: Serial number for additional validation';
COMMENT ON COLUMN certificate_role_mapping.override_role_id IS 'The role to assign (replaces default OU-based role)';
