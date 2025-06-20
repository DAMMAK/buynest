CREATE TABLE notification_logs (
                                   id BIGSERIAL PRIMARY KEY,
                                   user_id VARCHAR(255) NOT NULL,
                                   recipient VARCHAR(255) NOT NULL,
                                   type VARCHAR(50) NOT NULL,
                                   subject VARCHAR(500) NOT NULL,
                                   content TEXT,
                                   status VARCHAR(50) NOT NULL,
                                   template_id VARCHAR(255),
                                   error_message TEXT,
                                   retry_count INTEGER DEFAULT 0,
                                   scheduled_at TIMESTAMP,
                                   sent_at TIMESTAMP,
                                   metadata TEXT,
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notification_logs_user_id ON notification_logs(user_id);
CREATE INDEX idx_notification_logs_status ON notification_logs(status);
CREATE INDEX idx_notification_logs_type ON notification_logs(type);
CREATE INDEX idx_notification_logs_scheduled_at ON notification_logs(scheduled_at);
CREATE INDEX idx_notification_logs_created_at ON notification_logs(created_at);