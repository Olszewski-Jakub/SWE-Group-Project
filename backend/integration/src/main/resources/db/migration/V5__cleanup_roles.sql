-- Remove deprecated/legacy roles; align with domain enum
DELETE FROM user_roles ur USING roles r WHERE ur.role_id = r.id AND r.name NOT IN ('CUSTOMER','STAFF','MANAGER','ADMIN','SUPPORT');
DELETE FROM roles WHERE name NOT IN ('CUSTOMER','STAFF','MANAGER','ADMIN','SUPPORT');

-- Ensure all enum roles exist
INSERT INTO roles (id, name) SELECT gen_random_uuid(), 'CUSTOMER' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name='CUSTOMER');
INSERT INTO roles (id, name) SELECT gen_random_uuid(), 'STAFF'    WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name='STAFF');
INSERT INTO roles (id, name) SELECT gen_random_uuid(), 'MANAGER'  WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name='MANAGER');
INSERT INTO roles (id, name) SELECT gen_random_uuid(), 'ADMIN'    WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name='ADMIN');
INSERT INTO roles (id, name) SELECT gen_random_uuid(), 'SUPPORT'  WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name='SUPPORT');

