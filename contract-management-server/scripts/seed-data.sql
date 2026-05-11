-- Seed Data for Contract Management System
-- Run this script after the services are up to populate test data

-- ============================================
-- COLLABORATOR SERVICE DATA (Run on collaborator_db)
-- ============================================

-- Insert sample collaborators (using national_id as primary key)
INSERT INTO collaborators (national_id, employee_code, first_name, last_name, email, phone, department, position, status, hire_date, created_at, updated_at)
VALUES 
    ('1234567890', 'EMP001', 'John', 'Doe', 'john.doe@company.com', '+1234567890', 'Engineering', 'Senior Developer', 'ACTIVE', '2022-01-15', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('2345678901', 'EMP002', 'Jane', 'Smith', 'jane.smith@company.com', '+1234567891', 'Engineering', 'Tech Lead', 'ACTIVE', '2021-06-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('3456789012', 'EMP003', 'Bob', 'Johnson', 'bob.johnson@company.com', '+1234567892', 'Marketing', 'Marketing Manager', 'ACTIVE', '2023-03-20', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('4567890123', 'EMP004', 'Alice', 'Williams', 'alice.williams@company.com', '+1234567893', 'HR', 'HR Specialist', 'ACTIVE', '2022-09-10', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('5678901234', 'EMP005', 'Charlie', 'Brown', 'charlie.brown@company.com', '+1234567894', 'Finance', 'Financial Analyst', 'ON_HOLD', '2021-11-25', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (national_id) DO NOTHING;

-- Insert sample performance reviews
INSERT INTO performance_reviews (id, collaborator_id, reviewer_name, reviewer_email, review_period_start, review_period_end, rating, performance_category, strengths, areas_for_improvement, comments, is_eligible_renewal, created_at, updated_at)
VALUES 
    ('11111111-1111-1111-1111-111111111111', '1234567890', 'Sarah Manager', 'sarah.manager@company.com', '2024-01-01', '2024-06-30', 4.50, 'EXCEEDS_EXPECTATIONS', 'Excellent technical skills, great team player', 'Could improve documentation', 'Outstanding performance this period', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222222', '1234567890', 'Sarah Manager', 'sarah.manager@company.com', '2023-07-01', '2023-12-31', 4.00, 'MEETS_EXPECTATIONS', 'Good problem-solving abilities', 'Time management could be better', 'Solid performance', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333333', '2345678901', 'Mike Director', 'mike.director@company.com', '2024-01-01', '2024-06-30', 4.75, 'EXCEEDS_EXPECTATIONS', 'Exceptional leadership, innovative solutions', 'None noted', 'Highly recommended for promotion', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('44444444-4444-4444-4444-444444444444', '3456789012', 'Lisa VP', 'lisa.vp@company.com', '2024-01-01', '2024-06-30', 3.50, 'MEETS_EXPECTATIONS', 'Creative marketing campaigns', 'Budget management needs improvement', 'Good overall performance', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('55555555-5555-5555-5555-555555555555', '5678901234', 'Tom CFO', 'tom.cfo@company.com', '2024-01-01', '2024-06-30', 2.50, 'BELOW_EXPECTATIONS', 'Good analytical skills', 'Needs to improve accuracy and deadlines', 'Performance improvement plan recommended', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;


-- ============================================
-- CONTRACT SERVICE DATA (Run on contract_db)
-- ============================================

-- Insert sample contracts (using collaborator national IDs)
INSERT INTO contracts (id, contract_number, collaborator_id, contract_type, start_date, end_date, salary, currency, terms_and_conditions, status, renewal_count, auto_renewal, notice_period_days, created_at, updated_at)
VALUES 
    ('aaaa1111-bbbb-cccc-dddd-eeee11111111', 'CTR-2024-0001', '1234567890', 'FULL_TIME', '2024-01-01', '2025-01-01', 85000.00, 'USD', 'Standard employment contract with benefits package', 'ACTIVE', 1, true, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('bbbb2222-cccc-dddd-eeee-ffff22222222', 'CTR-2024-0002', '2345678901', 'FULL_TIME', '2024-01-01', '2024-12-31', 120000.00, 'USD', 'Tech Lead contract with equity options', 'ACTIVE', 2, true, 60, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('cccc3333-dddd-eeee-ffff-000033333333', 'CTR-2024-0003', '3456789012', 'FULL_TIME', '2024-03-01', '2025-03-01', 75000.00, 'USD', 'Marketing Manager contract', 'ACTIVE', 0, false, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('dddd4444-eeee-ffff-0000-111144444444', 'CTR-2024-0004', '4567890123', 'PART_TIME', '2024-01-01', '2024-06-30', 40000.00, 'USD', 'Part-time HR specialist contract', 'ACTIVE', 0, false, 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('eeee5555-ffff-0000-1111-222255555555', 'CTR-2023-0005', '5678901234', 'CONTRACTOR', '2023-06-01', '2024-06-01', 95000.00, 'USD', 'Contractor agreement for financial analysis', 'EXPIRED', 0, false, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;


-- ============================================
-- NOTIFICATION SERVICE DATA (Run on notification_db)
-- ============================================

-- Insert sample notifications (using collaborator national IDs)
INSERT INTO expiry_notifications (id, contract_id, collaborator_id, notification_type, recipient_email, recipient_name, subject, message_body, days_until_expiry, status, scheduled_at, created_at, updated_at)
VALUES 
    ('11110000-2222-3333-4444-555566667777', 'bbbb2222-cccc-dddd-eeee-ffff22222222', '2345678901', 'RENEWAL_REMINDER', 'jane.smith@company.com', 'Jane Smith', 'Contract Renewal Reminder - CTR-2024-0002', 'Your contract is expiring soon. Please contact HR for renewal.', 30, 'SENT', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP),
    ('22220000-3333-4444-5555-666677778888', 'dddd4444-eeee-ffff-0000-111144444444', '4567890123', 'EXPIRY_WARNING', 'alice.williams@company.com', 'Alice Williams', 'Contract Expiry Warning - CTR-2024-0004', 'Your contract will expire in 7 days. Immediate action required.', 7, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33330000-4444-5555-6666-777788889999', 'eeee5555-ffff-0000-1111-222255555555', '5678901234', 'EXPIRED_NOTICE', 'charlie.brown@company.com', 'Charlie Brown', 'Contract Expired - CTR-2023-0005', 'Your contract has expired. Please contact HR immediately.', 0, 'SENT', CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;
