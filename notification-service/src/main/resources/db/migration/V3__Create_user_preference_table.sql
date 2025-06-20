CREATE TABLE user_preferences (
                                  id BIGSERIAL PRIMARY KEY,
                                  user_id VARCHAR(255) NOT NULL,
                                  notification_type VARCHAR(50) NOT NULL,
                                  email_enabled BOOLEAN NOT NULL DEFAULT true,
                                  sms_enabled BOOLEAN NOT NULL DEFAULT false,
                                  push_enabled BOOLEAN NOT NULL DEFAULT true,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  UNIQUE(user_id, notification_type)
);

CREATE INDEX idx_user_preferences_user_id ON user_preferences(user_id);
CREATE INDEX idx_user_preferences_notification_type ON user_preferences(notification_type);