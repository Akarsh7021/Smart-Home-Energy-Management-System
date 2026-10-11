-- 01_create_tables.sql
-- Smart Home Energy Management System - Stage 1
-- Creates the three application tables used by the prototype.
-- Spring Session tables are managed separately by Spring Session JDBC.

CREATE TABLE IF NOT EXISTS users (
                                     user_id UUID PRIMARY KEY,
                                     name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
    CHECK (role IN ('HOMEOWNER', 'FAMILY_MEMBER')),
    can_control_devices BOOLEAN NOT NULL DEFAULT FALSE,
    can_view_energy BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS smart_devices (
                                             device_id UUID PRIMARY KEY,
                                             name VARCHAR(100) NOT NULL,
    device_type VARCHAR(50) NOT NULL,
    manufacturer VARCHAR(100),
    -- Stage 1 only supports a light bulb, so its current status is ON or OFF.
    status VARCHAR(20) NOT NULL
    CHECK (status IN ('ON', 'OFF')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS device_readings (
                                               reading_id UUID PRIMARY KEY,
                                               device_id UUID NOT NULL,
                                               recorded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- JSONB stores readings that can differ by device or measurement.
    -- Example: {"status":"ON","powerWatts":9.5,"energyKWh":0.0095}
                                               reading_data JSONB NOT NULL,

    -- Each reading belongs to one device. If that device is deleted,
    -- its historical readings are deleted as well.
                                               CONSTRAINT fk_device_readings_device
                                               FOREIGN KEY (device_id)
    REFERENCES smart_devices (device_id)
    ON DELETE CASCADE
    );

-- Speeds up queries that retrieve one device's readings in time order.
CREATE INDEX IF NOT EXISTS idx_device_readings_device_recorded_at
    ON device_readings (device_id, recorded_at DESC);