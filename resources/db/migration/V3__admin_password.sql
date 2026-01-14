-- Flyway migration V3
-- Ensure default admin credentials match "admin/admin" for local demo
-- NOTE: passwords are stored as BCrypt hashes in users.password_hash

UPDATE users
SET password_hash = '$2a$10$KcSRqwk45DrVvvVnStRnTO3HtAhGKykmNFWjs.6Xh7bBvvjO8gL7G',
    enabled = TRUE
WHERE username = 'admin';
