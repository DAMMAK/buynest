-- V1__Create_payment_table.sql

CREATE TABLE payments (
                          id BIGSERIAL PRIMARY KEY,
                          payment_id VARCHAR(255) UNIQUE NOT NULL,
                          order_id VARCHAR(255) NOT NULL,
                          user_id VARCHAR(255) NOT NULL,
                          amount DECIMAL(19,2) NOT NULL,
                          currency VARCHAR(3) NOT NULL,
                          status VARCHAR(50) NOT NULL,
                          payment_method VARCHAR(50) NOT NULL,
                          gateway_transaction_id VARCHAR(255),
                          gateway_payment_id VARCHAR(255),
                          gateway_response TEXT,
                          failure_reason VARCHAR(500),
                          retry_count INTEGER DEFAULT 0,
                          refunded_amount DECIMAL(19,2) DEFAULT 0.00,
                          ip_address VARCHAR(45),
                          user_agent TEXT,
                          fraud_score DECIMAL(5,2),
                          is_fraudulent BOOLEAN DEFAULT FALSE,
                          card_last_four VARCHAR(4),
                          card_brand VARCHAR(50),
                          paypal_email VARCHAR(255),
                          wallet_type VARCHAR(50),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          processed_at TIMESTAMP,
                          version BIGINT DEFAULT 0
);

-- Indexes for better performance
CREATE INDEX idx_payments_payment_id ON payments(payment_id);
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_created_at ON payments(created_at);
CREATE INDEX idx_payments_payment_method ON payments(payment_method);
CREATE INDEX idx_payments_fraud_score ON payments(fraud_score);

-- Add constraints
ALTER TABLE payments ADD CONSTRAINT chk_amount_positive CHECK (amount > 0);
ALTER TABLE payments ADD CONSTRAINT chk_refunded_amount_non_negative CHECK (refunded_amount >= 0);
ALTER TABLE payments ADD CONSTRAINT chk_currency_length CHECK (LENGTH(currency) = 3);