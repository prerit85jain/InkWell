-- Add token expiry column to existing subscribers table
ALTER TABLE subscribers ADD COLUMN IF NOT EXISTS token_expires_at DATETIME(6);

-- Add updated_at for tracking last modification
ALTER TABLE subscribers ADD COLUMN IF NOT EXISTS updated_at DATETIME(6);