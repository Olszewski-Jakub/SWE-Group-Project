-- Ensure SUPPORT role exists alongside existing seeds
INSERT INTO roles (id, name)
SELECT gen_random_uuid(), 'SUPPORT'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'SUPPORT');

