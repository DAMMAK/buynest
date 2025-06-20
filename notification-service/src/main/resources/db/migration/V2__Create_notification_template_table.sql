CREATE TABLE notification_templates (
                                        id BIGSERIAL PRIMARY KEY,
                                        template_id VARCHAR(255) UNIQUE NOT NULL,
                                        name VARCHAR(255) NOT NULL,
                                        type VARCHAR(50) NOT NULL,
                                        subject VARCHAR(500) NOT NULL,
                                        content TEXT NOT NULL,
                                        variables TEXT,
                                        active BOOLEAN DEFAULT true,
                                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notification_templates_template_id ON notification_templates(template_id);
CREATE INDEX idx_notification_templates_type ON notification_templates(type);
CREATE INDEX idx_notification_templates_active ON notification_templates(active);

-- Insert default templates
INSERT INTO notification_templates (template_id, name, type, subject, content, variables, active) VALUES
                                                                                                      ('order-confirmation', 'Order Confirmation', 'EMAIL', 'Order Confirmation - {{orderId}}',
                                                                                                       '<h1>Thank you for your order!</h1><p>Dear {{customerName}},</p><p>Your order {{orderId}} has been confirmed.</p><p>Total: ${{totalAmount}}</p>',
                                                                                                       'orderId,customerName,totalAmount,orderDate', true),
                                                                                                      ('shipping-update', 'Shipping Update', 'EMAIL', 'Your Order Has Shipped - {{orderId}}',
                                                                                                       '<h1>Your order is on its way!</h1><p>Order {{orderId}} has been shipped.</p><p>Tracking Number: {{trackingNumber}}</p><p>Estimated Delivery: {{estimatedDelivery}}</p>',
                                                                                                       'orderId,trackingNumber,estimatedDelivery', true),
                                                                                                      ('password-reset', 'Password Reset', 'EMAIL', 'Password Reset Request',
                                                                                                       '<h1>Password Reset</h1><p>Click the link below to reset your password:</p><p><a href="{{resetLink}}">Reset Password</a></p><p>This link expires in {{expiryTime}}.</p>',
                                                                                                       'resetLink,expiryTime', true);