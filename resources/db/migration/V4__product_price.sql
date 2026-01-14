-- Flyway migration V4
-- Add product price

ALTER TABLE products
    ADD COLUMN price DECIMAL(12,2) NOT NULL DEFAULT 0.00;
