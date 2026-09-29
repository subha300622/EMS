-- Flyway Migration V56: Comprehensive Training Management Expansion Tables

-- 1. Main Trainings Table
CREATE TABLE IF NOT EXISTS trainings (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100) NOT NULL,
    training_type VARCHAR(50) NOT NULL, -- TECHNICAL, KT, SOFT_SKILL, COMPLIANCE, LEARNING
    department_id BIGINT,
    team_id BIGINT,
    trainer_id BIGINT NOT NULL,
    delivery_method VARCHAR(50) NOT NULL, -- ONLINE, OFFLINE, HYBRID
    start_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    is_recurring BOOLEAN NOT NULL DEFAULT FALSE,
    meeting_link VARCHAR(500),
    venue VARCHAR(255),
    approval_required BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT', -- DRAFT, PENDING_APPROVAL, REJECTED, CHANGES_REQUESTED, APPROVED, PUBLISHED, ONGOING, COMPLETED, CANCELLED
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trainings_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_trainings_trainer FOREIGN KEY (trainer_id) REFERENCES employees(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_trainings_org_status ON trainings(organization_id, status);
CREATE INDEX IF NOT EXISTS idx_trainings_start_end ON trainings(start_date_time, end_date_time);

-- 2. Training Approval Audit History
CREATE TABLE IF NOT EXISTS training_approval_audits (
    id BIGSERIAL PRIMARY KEY,
    training_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL, -- SUBMIT, APPROVE, REJECT, REQUEST_CHANGES, CANCEL
    actor_id BIGINT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trn_approval_audit_training FOREIGN KEY (training_id) REFERENCES trainings(id) ON DELETE CASCADE
);

-- 3. Training Recurrence Configuration
CREATE TABLE IF NOT EXISTS training_recurrence_configs (
    id BIGSERIAL PRIMARY KEY,
    training_id BIGINT NOT NULL UNIQUE,
    frequency VARCHAR(50) NOT NULL, -- DAILY, WEEKLY, MONTHLY, CUSTOM
    interval_val INT NOT NULL DEFAULT 1,
    days_of_week VARCHAR(100), -- E.g., MONDAY,WEDNESDAY
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trn_recurrence_training FOREIGN KEY (training_id) REFERENCES trainings(id) ON DELETE CASCADE
);

-- 4. Training Discrete Sessions
DROP TABLE IF EXISTS training_sessions CASCADE;
CREATE TABLE IF NOT EXISTS training_sessions (
    id BIGSERIAL PRIMARY KEY,
    training_id BIGINT NOT NULL,
    session_number INT NOT NULL,
    title VARCHAR(255),
    start_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED', -- SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    meeting_link VARCHAR(500),
    venue VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trn_session_training FOREIGN KEY (training_id) REFERENCES trainings(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_trn_sessions_training ON training_sessions(training_id, session_number);

-- 5. Training Participants Table (Participation Lifecycle)
CREATE TABLE IF NOT EXISTS training_participants (
    id BIGSERIAL PRIMARY KEY,
    training_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    assignment_target_type VARCHAR(50) NOT NULL, -- EMPLOYEE, DEPARTMENT, TEAM, DESIGNATION
    target_id BIGINT,
    participation_status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, ASSIGNED, ACCEPTED, DECLINED
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP WITH TIME ZONE,
    response_note TEXT,
    CONSTRAINT fk_trn_participant_training FOREIGN KEY (training_id) REFERENCES trainings(id) ON DELETE CASCADE,
    CONSTRAINT fk_trn_participant_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT uq_trn_participant UNIQUE (training_id, employee_id)
);

CREATE INDEX IF NOT EXISTS idx_trn_participant_emp ON training_participants(employee_id, participation_status);

-- 6. Training Attendance Records (Attendance Lifecycle)
CREATE TABLE IF NOT EXISTS training_attendances (
    id BIGSERIAL PRIMARY KEY,
    training_id BIGINT NOT NULL,
    session_id BIGINT,
    employee_id BIGINT NOT NULL,
    attendance_status VARCHAR(50) NOT NULL DEFAULT 'ABSENT', -- ATTENDED, ABSENT, PARTIALLY_ATTENDED
    check_in_time TIMESTAMP WITH TIME ZONE,
    check_out_time TIMESTAMP WITH TIME ZONE,
    duration_minutes INT,
    marked_by BIGINT,
    remarks TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trn_attendance_training FOREIGN KEY (training_id) REFERENCES trainings(id) ON DELETE CASCADE,
    CONSTRAINT fk_trn_attendance_session FOREIGN KEY (session_id) REFERENCES training_sessions(id) ON DELETE SET NULL,
    CONSTRAINT fk_trn_attendance_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_trn_attendance_trn_emp ON training_attendances(training_id, employee_id);

-- 7. Training Materials (Course-specific)
CREATE TABLE IF NOT EXISTS training_materials (
    id BIGSERIAL PRIMARY KEY,
    training_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    material_type VARCHAR(50) NOT NULL, -- PDF, PPT, DOCUMENT, SOURCE_CODE, GIT_REPOSITORY, API_COLLECTION, EXTERNAL_LINK
    url_or_file_path TEXT NOT NULL,
    file_size_bytes BIGINT,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trn_materials_training FOREIGN KEY (training_id) REFERENCES trainings(id) ON DELETE CASCADE
);

-- 8. Training Library (Reusable Org-wide Learning Resources)
CREATE TABLE IF NOT EXISTS training_library_resources (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100) NOT NULL,
    technology VARCHAR(100),
    material_type VARCHAR(50) NOT NULL,
    resource_url TEXT NOT NULL,
    trainer_id BIGINT,
    department_id BIGINT,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trn_lib_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

-- 9. Training Feedback Table
CREATE TABLE IF NOT EXISTS training_feedbacks (
    id BIGSERIAL PRIMARY KEY,
    training_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    content_quality_rating INT CHECK (content_quality_rating >= 1 AND content_quality_rating <= 5),
    trainer_rating INT CHECK (trainer_rating >= 1 AND trainer_rating <= 5),
    overall_experience_rating INT CHECK (overall_experience_rating >= 1 AND overall_experience_rating <= 5),
    comments TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trn_feedback_training FOREIGN KEY (training_id) REFERENCES trainings(id) ON DELETE CASCADE,
    CONSTRAINT fk_trn_feedback_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT uq_trn_feedback UNIQUE (training_id, employee_id)
);
