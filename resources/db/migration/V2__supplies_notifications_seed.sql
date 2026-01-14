-- Flyway migration V2
-- Add supplies + notifications + extra indexes
-- Also add "min_stock_level" for products (compat with existing JPA column min_stock)

-- 1) Products: expose min_stock_level (requested by task)
-- Keep existing physical column min_stock (used by JPA), and add a generated alias column.
ALTER TABLE products
    ADD COLUMN min_stock_level INT GENERATED ALWAYS AS (min_stock) STORED;

-- 2) Supplies (incoming deliveries)
CREATE TABLE supplies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplier_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    reference_number VARCHAR(60) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RECEIVED', -- DRAFT/RECEIVED/CANCELLED
    supplied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_supplies_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    CONSTRAINT fk_supplies_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    CONSTRAINT fk_supplies_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT uk_supplies_reference UNIQUE (reference_number)
);

CREATE TABLE supply_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    supply_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_cost DECIMAL(12,2) NULL,
    CONSTRAINT fk_supply_items_supply FOREIGN KEY (supply_id) REFERENCES supplies(id) ON DELETE CASCADE,
    CONSTRAINT fk_supply_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- 3) Notifications
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NULL,
    notification_type VARCHAR(30) NOT NULL, -- e.g. LOW_STOCK / ORDER_STATUS / SYSTEM
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    related_product_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_notifications_product FOREIGN KEY (related_product_id) REFERENCES products(id) ON DELETE SET NULL
);

-- 4) Indexes (performance)
-- products
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_active ON products(active);

-- stock_items
CREATE INDEX idx_stock_items_product_id ON stock_items(product_id);
CREATE INDEX idx_stock_items_warehouse_id ON stock_items(warehouse_id);

-- orders
CREATE INDEX idx_orders_type_status ON orders(order_type, status);
CREATE INDEX idx_orders_warehouse_id ON orders(warehouse_id);
CREATE INDEX idx_orders_supplier_id ON orders(supplier_id);
CREATE INDEX idx_orders_created_by ON orders(created_by);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- order_items
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- supplies
CREATE INDEX idx_supplies_supplier_id ON supplies(supplier_id);
CREATE INDEX idx_supplies_warehouse_id ON supplies(warehouse_id);
CREATE INDEX idx_supplies_created_by ON supplies(created_by);
CREATE INDEX idx_supplies_supplied_at ON supplies(supplied_at);

-- supply_items
CREATE INDEX idx_supply_items_supply_id ON supply_items(supply_id);
CREATE INDEX idx_supply_items_product_id ON supply_items(product_id);

-- notifications
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read_created_at ON notifications(is_read, created_at);
CREATE INDEX idx_notifications_related_product_id ON notifications(related_product_id);


-- 5) Seed data (minimal demo data)

-- Roles already seeded in V1.

-- Users (bcrypt hashes; demo password for all users = "password")
-- Hash below is a well-known BCrypt for string "password".
INSERT INTO users (id, username, password_hash, enabled)
VALUES
  (1, 'admin',    '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5VQbPpL0IuV9i1N/sz7./bZrj6T2y', TRUE),
  (2, 'manager',  '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5VQbPpL0IuV9i1N/sz7./bZrj6T2y', TRUE),
  (3, 'employee', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5VQbPpL0IuV9i1N/sz7./bZrj6T2y', TRUE);

-- User roles
INSERT INTO user_roles(user_id, role_id)
SELECT 1, id FROM roles WHERE name IN ('ROLE_ADMIN','ROLE_MANAGER','ROLE_EMPLOYEE');
INSERT INTO user_roles(user_id, role_id)
SELECT 2, id FROM roles WHERE name IN ('ROLE_MANAGER','ROLE_EMPLOYEE');
INSERT INTO user_roles(user_id, role_id)
SELECT 3, id FROM roles WHERE name IN ('ROLE_EMPLOYEE');

-- Warehouses
INSERT INTO warehouses (id, name, address, active) VALUES
  (1, 'Main warehouse', 'Moscow, Lenina 1', TRUE),
  (2, 'Spare warehouse', 'Moscow, Tverskaya 10', TRUE);

-- Suppliers
INSERT INTO suppliers (id, name, phone, email, address) VALUES
  (1, 'OOO Postavshik-1', '+7-495-000-00-01', 's1@example.com', 'Moscow'),
  (2, 'IP Ivanov',       '+7-495-000-00-02', 'ivanov@example.com', 'Khimki');

-- Categories
INSERT INTO categories (id, name) VALUES
  (1, 'Electronics'),
  (2, 'Office'),
  (3, 'Food');

-- Products (min_stock is the real column; min_stock_level is generated)
INSERT INTO products (id, sku, name, description, unit, min_stock, category_id, active) VALUES
  (1, 'EL-0001', 'USB Cable', 'USB-A to USB-C', 'pcs', 10, 1, TRUE),
  (2, 'OF-0001', 'Paper A4',  '80 gsm',        'pack', 5,  2, TRUE),
  (3, 'FD-0001', 'Coffee',    'Arabica 1kg',   'kg',   2,  3, TRUE);

-- Stock items (balances)
INSERT INTO stock_items (id, warehouse_id, product_id, quantity) VALUES
  (1, 1, 1, 25),
  (2, 1, 2, 3),
  (3, 2, 3, 8);

-- Supplies
INSERT INTO supplies (id, supplier_id, warehouse_id, reference_number, status, supplied_at, created_by)
VALUES
  (1, 1, 1, 'SUP-2025-0001', 'RECEIVED', CURRENT_TIMESTAMP, 2),
  (2, 2, 2, 'SUP-2025-0002', 'RECEIVED', CURRENT_TIMESTAMP, 2);

INSERT INTO supply_items (id, supply_id, product_id, quantity, unit_cost) VALUES
  (1, 1, 2, 20, 350.00),
  (2, 2, 3, 5,  1200.00);

-- Orders (existing tables from V1)
INSERT INTO orders (id, order_type, status, warehouse_id, supplier_id, customer_name, created_by)
VALUES
  (1, 'PURCHASE', 'CONFIRMED', 1, 1, NULL, 2),
  (2, 'SALES',    'DRAFT',     1, NULL, 'OOO Customer', 3);

INSERT INTO order_items (id, order_id, product_id, quantity, price) VALUES
  (1, 1, 1, 10, 150.00),
  (2, 2, 3,  2, 2000.00);

-- Notifications
INSERT INTO notifications (id, user_id, notification_type, title, message, related_product_id, is_read)
VALUES
  (1, 2, 'LOW_STOCK', 'Low stock: Paper A4', 'Quantity is below min_stock_level', 2, FALSE),
  (2, 1, 'SYSTEM',    'Welcome',            'Inventory DB initialized',          NULL, TRUE);
