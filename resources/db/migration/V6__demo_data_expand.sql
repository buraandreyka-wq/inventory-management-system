-- Flyway migration V6
-- Expanded demo data: 3 products per category, multiple supplies, stock balances, and "my orders".
-- NOTE: This migration assumes V2 seeded base entities with fixed IDs.

-- === Products: add more items so each category has at least 3 products ===
-- Electronics (category_id = 1)
INSERT INTO products (id, sku, name, description, unit, min_stock, category_id, active, price) VALUES
  (4, 'EL-0002', 'Power Bank 10000', 'Portable battery 10000mAh', 'pcs', 5, 1, TRUE, 1990.00),
  (5, 'EL-0003', 'HDMI Cable 2m', 'HDMI 2.0 2 meters',        'pcs', 8, 1, TRUE, 390.00);

-- Office (category_id = 2)
INSERT INTO products (id, sku, name, description, unit, min_stock, category_id, active, price) VALUES
  (6, 'OF-0002', 'Stapler',      'Metal stapler',      'pcs', 2, 2, TRUE, 450.00),
  (7, 'OF-0003', 'Ballpen Blue', 'Blue ink pen',       'pcs', 30, 2, TRUE, 25.00);

-- Food (category_id = 3)
INSERT INTO products (id, sku, name, description, unit, min_stock, category_id, active, price) VALUES
  (8, 'FD-0002', 'Tea',   'Black tea 500g',  'kg', 1, 3, TRUE, 850.00),
  (9, 'FD-0003', 'Sugar', 'White sugar 1kg', 'kg', 5, 3, TRUE, 95.00);

-- === Stock balances: ensure there are stock_items for most products in both warehouses ===
-- Warehouse 1 (id=1)
INSERT INTO stock_items (warehouse_id, product_id, quantity) VALUES
  (1, 3, 15),
  (1, 4, 12),
  (1, 5, 40),
  (1, 6, 7),
  (1, 7, 200),
  (1, 8, 9),
  (1, 9, 35)
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity);

-- Warehouse 2 (id=2)
INSERT INTO stock_items (warehouse_id, product_id, quantity) VALUES
  (2, 1, 10),
  (2, 2, 25),
  (2, 4, 5),
  (2, 6, 3),
  (2, 7, 120),
  (2, 8, 4),
  (2, 9, 18)
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity);

-- === Supplies: add a few more delivered supplies and items ===
INSERT INTO supplies (id, supplier_id, warehouse_id, reference_number, status, supplied_at, created_by)
VALUES
  (3, 1, 1, 'SUP-2025-0003', 'RECEIVED', CURRENT_TIMESTAMP, 2),
  (4, 2, 1, 'SUP-2025-0004', 'RECEIVED', CURRENT_TIMESTAMP, 2),
  (5, 2, 2, 'SUP-2025-0005', 'RECEIVED', CURRENT_TIMESTAMP, 2);

INSERT INTO supply_items (id, supply_id, product_id, quantity, unit_cost) VALUES
  (3, 3, 4, 20, 1500.00),
  (4, 3, 7, 300, 18.00),
  (5, 4, 8, 10, 600.00),
  (6, 4, 9, 50, 70.00),
  (7, 5, 2, 40, 320.00),
  (8, 5, 6, 10, 350.00);

-- === Orders: add "my orders" for employee (user_id = 3) ===
-- Create a couple of confirmed sales orders and one draft.
INSERT INTO orders (id, order_type, status, warehouse_id, supplier_id, customer_name, created_by)
VALUES
  (3, 'SALES', 'CONFIRMED', 1, NULL, 'ООО Ромашка', 3),
  (4, 'SALES', 'CONFIRMED', 2, NULL, 'ИП Петров',   3),
  (5, 'SALES', 'DRAFT',     1, NULL, 'Тестовый',    3);

INSERT INTO order_items (id, order_id, product_id, quantity, price) VALUES
  (3, 3, 7, 20,  25.00),
  (4, 3, 5,  3, 390.00),
  (5, 4, 2,  5,  0.00),
  (6, 4, 3,  1,  0.00),
  (7, 5, 1,  1,  0.00);

-- Stock movements (for analytics/report history). Not strictly required for balances,
-- but useful for "движения" report and completeness.
INSERT INTO stock_movements (movement_type, source_order_id, warehouse_id, product_id, quantity, created_by, created_at)
VALUES
  ('OUT', 3, 1, 7, 20, 3, CURRENT_TIMESTAMP),
  ('OUT', 3, 1, 5,  3, 3, CURRENT_TIMESTAMP),
  ('OUT', 4, 2, 2,  5, 3, CURRENT_TIMESTAMP),
  ('OUT', 4, 2, 3,  1, 3, CURRENT_TIMESTAMP);
