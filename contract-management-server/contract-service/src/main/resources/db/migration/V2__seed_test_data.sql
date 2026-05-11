-- Seed Test Data for Contract Service
-- This runs automatically on startup via Flyway

-- Insert sample contracts (using collaborator national IDs)
INSERT INTO contracts (id, contract_number, collaborator_id, contract_type, start_date, end_date, salary, currency, terms_and_conditions, status, renewal_count, auto_renewal, notice_period_days, created_at, updated_at)
VALUES 
    ('aaaa1111-bbbb-cccc-dddd-eeee11111111', 'CTR-2024-0001', '1234567890', 'FULL_TIME', '2024-01-01', '2025-01-01', 85000.00, 'USD', 'Standard employment contract with benefits package', 'ACTIVE', 1, true, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('bbbb2222-cccc-dddd-eeee-ffff22222222', 'CTR-2024-0002', '2345678901', 'FULL_TIME', '2024-01-01', '2024-12-31', 120000.00, 'USD', 'Tech Lead contract with equity options', 'ACTIVE', 2, true, 60, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('cccc3333-dddd-eeee-ffff-000033333333', 'CTR-2024-0003', '3456789012', 'FULL_TIME', '2024-03-01', '2025-03-01', 75000.00, 'USD', 'Marketing Manager contract', 'ACTIVE', 0, false, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('dddd4444-eeee-ffff-0000-111144444444', 'CTR-2024-0004', '4567890123', 'PART_TIME', '2024-01-01', '2024-06-30', 40000.00, 'USD', 'Part-time HR specialist contract', 'ACTIVE', 0, false, 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('eeee5555-ffff-0000-1111-222255555555', 'CTR-2023-0005', '5678901234', 'CONTRACTOR', '2023-06-01', '2024-06-01', 95000.00, 'USD', 'Contractor agreement for financial analysis', 'EXPIRED', 0, false, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;
