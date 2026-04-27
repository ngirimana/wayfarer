-- Add bio to users
ALTER TABLE users ADD COLUMN IF NOT EXISTS bio TEXT;
