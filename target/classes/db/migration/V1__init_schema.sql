CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    target_role VARCHAR(100),
    target_companies JSONB,
    experience_level VARCHAR(20) CHECK (experience_level IN ('FRESHER','JUNIOR','MID','SENIOR')),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE resumes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    parsed_text TEXT,
    parsed_json JSONB,
    ats_score INTEGER CHECK (ats_score BETWEEN 0 AND 100),
    version INTEGER NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE job_applications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company_name VARCHAR(255) NOT NULL,
    role_title VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'WISHLIST'
        CHECK (status IN ('WISHLIST','APPLIED','SCREENING','INTERVIEW','OFFER','REJECTED')),
    jd_url VARCHAR(1000),
    jd_text TEXT,
    applied_date DATE,
    notes TEXT,
    follow_up_date DATE,
    ats_score_at_apply INTEGER,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_resumes_user_id ON resumes(user_id);
CREATE INDEX idx_job_applications_user_id ON job_applications(user_id);