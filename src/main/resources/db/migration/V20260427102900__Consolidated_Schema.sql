-- Consolidated Schema 20260427102900
-- This consolidates initial creation and audit fields to bypass baseline issues.

CREATE SEQUENCE IF NOT EXISTS hibernate_sequence START WITH 1 INCREMENT BY 1;

-- Users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    isVerified BOOLEAN DEFAULT FALSE,
    otp VARCHAR(255),
    otpExpiry TIMESTAMP
);
ALTER TABLE users ADD COLUMN IF NOT EXISTS createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Trips
CREATE TABLE IF NOT EXISTS trips (
    id BIGINT PRIMARY KEY,
    origin VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    tripDate TIMESTAMP NOT NULL,
    fare DOUBLE PRECISION NOT NULL,
    capacity INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL
);
ALTER TABLE trips ADD COLUMN IF NOT EXISTS createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE trips ADD COLUMN IF NOT EXISTS updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Bookings
CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    trip_id BIGINT NOT NULL,
    seatNumber INTEGER NOT NULL
);

DO $$ 
BEGIN 
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='bookings' AND column_name='bookingdate') THEN
        ALTER TABLE bookings RENAME COLUMN bookingDate TO createdAt;
    ELSE
        ALTER TABLE bookings ADD COLUMN IF NOT EXISTS createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;

ALTER TABLE bookings ADD COLUMN IF NOT EXISTS updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Constraints
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name='fk_booking_user') THEN
        ALTER TABLE bookings ADD CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name='fk_booking_trip') THEN
        ALTER TABLE bookings ADD CONSTRAINT fk_booking_trip FOREIGN KEY (trip_id) REFERENCES trips(id);
    END IF;
END $$;
