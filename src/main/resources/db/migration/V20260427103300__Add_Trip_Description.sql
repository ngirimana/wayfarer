-- Add description to trips
ALTER TABLE trips ADD COLUMN IF NOT EXISTS description TEXT;
