-- Flyway migration V4
-- Admin features: system logs + system settings

CREATE TABLE system_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    level VARCHAR(10) NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    message VARCHAR(500) NOT NULL,
    actor VARCHAR(80) NULL,
    entity_type VARCHAR(80) NULL,
    entity_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_system_logs_created_at ON system_logs(created_at);
CREATE INDEX idx_system_logs_event_type ON system_logs(event_type);

CREATE TABLE system_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(120) NOT NULL UNIQUE,
    setting_value VARCHAR(500) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Default settings
INSERT INTO system_settings(setting_key, setting_value) VALUES ('report.topProducts.limit', '10');
INSERT INTO system_settings(setting_key, setting_value) VALUES ('order.defaultWarehouseId', '');
