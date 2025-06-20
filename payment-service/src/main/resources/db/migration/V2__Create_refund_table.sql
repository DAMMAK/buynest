CREATE TABLE refunds (
                         id BIGSERIAL PRIMARY KEY,
                         refund_id VARCHAR(255) UNIQUE NOT NULL,
                         payment_id BIGINT NOT NULL,
                         amount DECIMAL(19,2) NOT NULL,
                         status VARCHAR(50) NOT NULL,
                         reason VARCHAR(500),
                         gateway_refund_id VARCHAR(255),
                         gateway_response TEXT,
                         failure_reason VARCHAR(500),
                         initiated_by VARCHAR(255),
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         processed_at TIMESTAMP,

                         CONSTRAINT fk_refunds_payment FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE
);

-- Indexes for better performance
CREATE INDEX idx_refunds_refund_id ON refunds(refund_id);
CREATE INDEX idx_refunds_payment_id ON refunds(payment_id);
CREATE INDEX idx_refunds_status ON refunds(status);
CREATE INDEX idx_refunds_created_at ON refunds(created_at);

-- Add constraints
ALTER TABLE refunds ADD CONSTRAINT chk_refund_amount_positive CHECK (amount > 0);

-- Trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_payments_updated_at BEFORE UPDATE ON payments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_refunds_updated_at BEFORE UPDATE ON refunds
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

