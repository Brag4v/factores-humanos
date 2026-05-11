-- Seed Test Data for Collaborator Service
-- This runs automatically on startup via Flyway

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
