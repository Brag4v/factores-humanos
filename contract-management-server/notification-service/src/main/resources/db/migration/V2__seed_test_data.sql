-- Seed Test Data for Notification Service
-- This runs automatically on startup via Flyway

-- Insert sample notifications (using collaborator national IDs)
INSERT INTO expiry_notifications (id, contract_id, collaborator_id, notification_type, recipient_email, recipient_name, subject, message_body, days_until_expiry, status, scheduled_at, created_at, updated_at)
VALUES 
    ('11110000-2222-3333-4444-555566667777', 'bbbb2222-cccc-dddd-eeee-ffff22222222', '2345678901', 'RENEWAL_REMINDER', 'jane.smith@company.com', 'Jane Smith', 'Contract Renewal Reminder - CTR-2024-0002', 'Dear Jane Smith,

Your contract (CTR-2024-0002) is expiring soon. Please contact HR for renewal.

Best regards,
Contract Management System', 30, 'SENT', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP),
    ('22220000-3333-4444-5555-666677778888', 'dddd4444-eeee-ffff-0000-111144444444', '4567890123', 'EXPIRY_WARNING', 'alice.williams@company.com', 'Alice Williams', 'Contract Expiry Warning - CTR-2024-0004', 'Dear Alice Williams,

URGENT: Your contract will expire in 7 days. Please contact HR immediately.

Best regards,
Contract Management System', 7, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33330000-4444-5555-6666-777788889999', 'eeee5555-ffff-0000-1111-222255555555', '5678901234', 'EXPIRED_NOTICE', 'charlie.brown@company.com', 'Charlie Brown', 'Contract Expired - CTR-2023-0005', 'Dear Charlie Brown,

Your contract has expired. Please contact HR immediately to discuss your status.

Best regards,
Contract Management System', 0, 'SENT', CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;
