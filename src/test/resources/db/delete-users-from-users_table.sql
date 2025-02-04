DELETE FROM users_roles WHERE user_id IN (SELECT id FROM users);
DELETE FROM users;
