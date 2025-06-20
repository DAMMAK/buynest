CREATE TABLE carts (
                       id BIGSERIAL PRIMARY KEY,
                       session_id VARCHAR(255) UNIQUE,
                       user_id BIGINT,
                       subtotal DECIMAL(10, 2) DEFAULT 0.00,
                       tax_amount DECIMAL(10, 2) DEFAULT 0.00,
                       discount_amount DECIMAL(10, 2) DEFAULT 0.00,
                       total DECIMAL(10, 2) DEFAULT 0.00,
                       tax_rate DECIMAL(5, 4) DEFAULT 0.08,
                       is_active BOOLEAN DEFAULT true,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       expires_at TIMESTAMP
);

CREATE INDEX idx_carts_session_id ON carts(session_id);
CREATE INDEX idx_carts_user_id ON carts(user_id);
CREATE INDEX idx_carts_is_active ON carts(is_active);
CREATE INDEX idx_carts_expires_at ON carts(expires_at);