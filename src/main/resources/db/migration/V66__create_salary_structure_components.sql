-- Flyway Migration V66: Create Salary Structure Components and Calculation Rules Mapping

CREATE TABLE IF NOT EXISTS salary_structure_components (
    id BIGSERIAL PRIMARY KEY,
    salary_structure_id BIGINT NOT NULL REFERENCES salary_structures(id),
    salary_component_id BIGINT NOT NULL REFERENCES salary_components(id),
    calculation_type VARCHAR(50) NOT NULL,
    calculation_base_type VARCHAR(50) NOT NULL DEFAULT 'NONE',
    calculation_base_component_id BIGINT REFERENCES salary_components(id),
    fixed_amount NUMERIC(38,2),
    percentage NUMERIC(10,4),
    formula TEXT,
    calculation_order INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_salary_structure_component UNIQUE (salary_structure_id, salary_component_id)
);

CREATE INDEX IF NOT EXISTS idx_structure_components_struct_order ON salary_structure_components (salary_structure_id, calculation_order);
CREATE INDEX IF NOT EXISTS idx_structure_components_comp ON salary_structure_components (salary_component_id);
CREATE INDEX IF NOT EXISTS idx_structure_components_base ON salary_structure_components (calculation_base_component_id);
