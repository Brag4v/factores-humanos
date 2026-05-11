-- Collaborator Service Database Schema
-- Version: 1.0

-- Create collaborators table with national_id as primary key
CREATE TABLE IF NOT EXISTS collaborators (
    national_id VARCHAR(20) PRIMARY KEY,
    employee_code VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    department VARCHAR(100),
    position VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    hire_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    CONSTRAINT chk_collaborator_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'ON_HOLD'))
);

-- Create performance_reviews table
CREATE TABLE IF NOT EXISTS performance_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    collaborator_id VARCHAR(20) NOT NULL,
    reviewer_name VARCHAR(200) NOT NULL,
    reviewer_email VARCHAR(255),
    review_period_start DATE NOT NULL,
    review_period_end DATE NOT NULL,
    rating DECIMAL(3,2) NOT NULL,
    performance_category VARCHAR(30) NOT NULL,
    strengths TEXT,
    areas_for_improvement TEXT,
    comments TEXT,
    is_eligible_renewal BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_performance_review_collaborator 
        FOREIGN KEY (collaborator_id) REFERENCES collaborators(national_id) ON DELETE CASCADE,
    CONSTRAINT chk_rating_range CHECK (rating >= 1.00 AND rating <= 5.00),
    CONSTRAINT chk_performance_category CHECK (
        performance_category IN ('EXCEEDS_EXPECTATIONS', 'MEETS_EXPECTATIONS', 'BELOW_EXPECTATIONS', 'NEEDS_IMPROVEMENT')
    ),
    CONSTRAINT chk_review_period CHECK (review_period_end >= review_period_start)
);

-- Create indexes for collaborators
CREATE INDEX IF NOT EXISTS idx_collaborator_email ON collaborators(email);
CREATE INDEX IF NOT EXISTS idx_collaborator_employee_code ON collaborators(employee_code);
CREATE INDEX IF NOT EXISTS idx_collaborator_status ON collaborators(status);
CREATE INDEX IF NOT EXISTS idx_collaborator_department ON collaborators(department);
CREATE INDEX IF NOT EXISTS idx_collaborator_hire_date ON collaborators(hire_date);

-- Create indexes for performance_reviews
CREATE INDEX IF NOT EXISTS idx_review_collaborator ON performance_reviews(collaborator_id);
CREATE INDEX IF NOT EXISTS idx_review_period_end ON performance_reviews(review_period_end);
CREATE INDEX IF NOT EXISTS idx_review_rating ON performance_reviews(rating);
CREATE INDEX IF NOT EXISTS idx_review_eligible_renewal ON performance_reviews(is_eligible_renewal);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at
DROP TRIGGER IF EXISTS update_collaborators_updated_at ON collaborators;
CREATE TRIGGER update_collaborators_updated_at
    BEFORE UPDATE ON collaborators
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_performance_reviews_updated_at ON performance_reviews;
CREATE TRIGGER update_performance_reviews_updated_at
    BEFORE UPDATE ON performance_reviews
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
