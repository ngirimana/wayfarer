-- Add fare_paid and unique constraint to bookings
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS fare_paid DOUBLE PRECISION NOT NULL DEFAULT 0.0;
ALTER TABLE bookings ADD CONSTRAINT unique_trip_seat UNIQUE (trip_id, seatNumber);
