ALTER TABLE onboarding_documents ADD COLUMN file_path VARCHAR(500);
ALTER TABLE onboarding_documents ADD COLUMN file_size BIGINT;
ALTER TABLE onboarding_documents ADD COLUMN uploaded_by VARCHAR(100);
