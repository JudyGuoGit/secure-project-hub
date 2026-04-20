-- Seed certificate-to-role mappings for testing
-- This demonstrates how to escalate privileges for specific client certificates

-- First, get the ADMIN role ID (assuming it exists from V7__Insert_seed_data.sql)
-- Then we can add certificate mappings if needed

-- Example: Uncomment below to grant specific certificates admin access
-- INSERT INTO certificate_role_mapping (certificate_cn, override_role_id, created_by, notes)
-- VALUES ('admin-client', (SELECT id FROM roles WHERE name = 'ADMIN'), 'system', 'Admin certificate');

-- For now, no seed data - admins should add mappings as needed via API or manually
