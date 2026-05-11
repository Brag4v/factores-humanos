-- Notification Service Database Schema
-- Version: 1.0

-- Create expiry_notifications table
CREATE TABLE IF NOT EXISTS expiry_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_id UUID NOT NULL,
    collaborator_id VARCHAR(20) NOT NULL,
    notification_type VARCHAR(30) NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    recipient_name VARCHAR(200) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    message_body TEXT NOT NULL,
    days_until_expiry INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMP WITH TIME ZONE,
    failure_reason TEXT,
    retry_count INTEGER DEFAULT 0,
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_notification_type CHECK (notification_type IN ('EXPIRY_WARNING', 'RENEWAL_REMINDER', 'EXPIRED_NOTICE')),
    CONSTRAINT chk_notification_status CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'CANCELLED'))
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_notification_contract ON expiry_notifications(contract_id);
CREATE INDEX IF NOT EXISTS idx_notification_collaborator ON expiry_notifications(collaborator_id);
CREATE INDEX IF NOT EXISTS idx_notification_status ON expiry_notifications(status);
CREATE INDEX IF NOT EXISTS idx_notification_scheduled ON expiry_notifications(scheduled_at);
CREATE INDEX IF NOT EXISTS idx_notification_type ON expiry_notifications(notification_type);
CREATE INDEX IF NOT EXISTS idx_notification_created ON expiry_notifications(created_at);

-- Composite index for finding pending notifications to send
CREATE INDEX IF NOT EXISTS idx_notification_pending_scheduled 
ON expiry_notifications(status, scheduled_at) 
WHERE status = 'PENDING';

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger for updated_at
DROP TRIGGER IF EXISTS update_expiry_notifications_updated_at ON expiry_notifications;
CREATE TRIGGER update_expiry_notifications_updated_at
    BEFORE UPDATE ON expiry_notifications
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
