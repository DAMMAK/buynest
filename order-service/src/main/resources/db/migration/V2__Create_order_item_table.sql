CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL,
                             product_name VARCHAR(255) NOT NULL,
                             product_sku VARCHAR(100) NOT NULL,
                             quantity INTEGER NOT NULL,
                             unit_price DECIMAL(19,2) NOT NULL,
                             total_price DECIMAL(19,2) NOT NULL,
                             discount_amount DECIMAL(19,2) DEFAULT 0,
                             product_image TEXT,
                             product_description TEXT,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_order_item_order_id ON order_items(order_id);
CREATE INDEX idx_order_item_product_id ON order_items(product_id);