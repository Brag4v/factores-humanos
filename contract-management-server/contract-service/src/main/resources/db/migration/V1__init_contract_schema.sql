-- Contract Service Database Schema
-- Version: 1.0

-- Create contracts table
CREATE TABLE IF NOT EXISTS contracts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_number VARCHAR(50) NOT NULL UNIQUE,
    collaborator_id VARCHAR(20) NOT NULL,
    contract_type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    salary DECIMAL(12,2),
    currency VARCHAR(3) DEFAULT 'USD',
    terms_and_conditions TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    previous_contract_id UUID,
    renewal_count INTEGER DEFAULT 0,
    auto_renewal BOOLEAN DEFAULT FALSE,
    notice_period_days INTEGER DEFAULT 30,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    CONSTRAINT chk_contract_type CHECK (contract_type IN ('FULL_TIME', 'PART_TIME', 'CONTRACTOR', 'TEMPORARY')),
    CONSTRAINT chk_contract_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'RENEWED', 'TERMINATED', 'PENDING')),
    CONSTRAINT chk_contract_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_salary_positive CHECK (salary IS NULL OR salary >= 0),
    CONSTRAINT fk_previous_contract FOREIGN KEY (previous_contract_id) REFERENCES contracts(id)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_contract_collaborator ON contracts(collaborator_id);
CREATE INDEX IF NOT EXISTS idx_contract_status ON contracts(status);
CREATE INDEX IF NOT EXISTS idx_contract_end_date ON contracts(end_date);
CREATE INDEX IF NOT EXISTS idx_contract_number ON contracts(contract_number);
CREATE INDEX IF NOT EXISTS idx_contract_type ON contracts(contract_type);
CREATE INDEX IF NOT EXISTS idx_contract_previous ON contracts(previous_contract_id);

-- Create unique partial index for active contracts per collaborator
-- This ensures only one active/pending contract per collaborator
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_active_contract_per_collaborator 
ON contracts(collaborator_id) 
WHERE status IN ('ACTIVE', 'PENDING');

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger for updated_at
DROP TRIGGER IF EXISTS update_contracts_updated_at ON contracts;
CREATE TRIGGER update_contracts_updated_at
    BEFORE UPDATE ON contracts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
