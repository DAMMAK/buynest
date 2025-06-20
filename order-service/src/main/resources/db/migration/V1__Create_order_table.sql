CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        order_number VARCHAR(50) UNIQUE NOT NULL,
                        user_id VARCHAR(50) NOT NULL,
                        status VARCHAR(20) NOT NULL,
                        total_amount DECIMAL(19,2) NOT NULL,
                        subtotal DECIMAL(19,2) NOT NULL,
                        tax_amount DECIMAL(19,2) NOT NULL,
                        shipping_amount DECIMAL(19,2) NOT NULL,
                        discount_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
                        coupon_code VARCHAR(50),
                        shipping_address TEXT NOT NULL,
                        billing_address TEXT NOT NULL,
                        payment_method VARCHAR(50) NOT NULL,
                        payment_transaction_id VARCHAR(100),
                        notes TEXT,
                        expected_delivery_date TIMESTAMP,
                        shipped_at TIMESTAMP,
                        delivered_at TIMESTAMP,
                        cancelled_at TIMESTAMP,
                        cancellation_reason TEXT,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        version BIGINT NOT NULL DEFAULT 0
);

-- Create indexes
CREATE INDEX idx_order_user_id ON orders(user_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_created_at ON orders(created_at);
CREATE INDEX idx_order_user_status ON orders(user_id, status);
CREATE INDEX idx_order_order_number ON orders(order_number);