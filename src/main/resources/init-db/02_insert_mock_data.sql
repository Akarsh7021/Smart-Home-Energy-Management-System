-- 02_insert_mock_data.sql
-- Smart Home Energy Management System - Stage 1
-- Adds sample users, one simulated smart light, and 21 days of hourly readings.
-- This script inserts data only; it does not create or alter tables.
-- Spring Session tables are not included.

BEGIN;

-- Three accounts let the prototype test the Homeowner role,
-- a permitted Family Member, and a Family Member without permissions.
INSERT INTO users
(user_id, name, email, password_hash, role,
 can_control_devices, can_view_energy)
VALUES
    ('11111111-1111-4111-8111-111111111111',
     'Alex Homeowner', 'homeowner@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'HOMEOWNER', TRUE, TRUE),

    ('22222222-2222-4222-8222-222222222222',
     'Sam Family Member', 'family@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'FAMILY_MEMBER', TRUE, TRUE),

    ('33333333-3333-4333-8333-333333333333',
     'Taylor Restricted Member', 'restricted@example.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'FAMILY_MEMBER', FALSE, FALSE)
    ON CONFLICT (user_id) DO UPDATE SET
    name = EXCLUDED.name,
                                 email = EXCLUDED.email,
                                 password_hash = EXCLUDED.password_hash,
                                 role = EXCLUDED.role,
                                 can_control_devices = EXCLUDED.can_control_devices,
                                 can_view_energy = EXCLUDED.can_view_energy;

-- Insert exactly one smart device for the Stage 1 prototype.
INSERT INTO smart_devices
(device_id, name, device_type, manufacturer, status)
VALUES
    ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
     'Living Room Smart Light', 'SMART_LIGHT',
     'Demo Manufacturer', 'ON')
    ON CONFLICT (device_id) DO UPDATE SET
    name = EXCLUDED.name,
                                   device_type = EXCLUDED.device_type,
                                   manufacturer = EXCLUDED.manufacturer,
                                   status = EXCLUDED.status;

-- Generate one reading per hour over 21 days:
-- 21 days * 24 hours = 504 readings for the same light.
--
-- The values are simulated, not measured from physical hardware.
-- The light is ON from 07:00 through 22:00 (9.5 W, 75% brightness)
-- and OFF during the remaining hours (0 W).
-- energyKWh is energy consumed during that individual hour, not a
-- cumulative meter value. 9.5 W for one hour = 0.0095 kWh.
--
-- generate_series() creates timestamps at one-hour intervals.
-- The range below includes both endpoints: Sep 18 at 00:00 through
-- Oct 8 at 23:00 in Vancouver time, giving exactly 504 timestamps.
INSERT INTO device_readings
(reading_id, device_id, recorded_at, reading_data)
SELECT
    -- Build a repeatable UUID-shaped ID from each timestamp. This keeps
    -- each hourly reading's ID stable if this mock-data script is rerun.
    (
        substr(md5('light-reading-' || hour_point::text), 1, 8) || '-' ||
        substr(md5('light-reading-' || hour_point::text), 9, 4) || '-' ||
        '4' || substr(md5('light-reading-' || hour_point::text), 14, 3) || '-' ||
        '8' || substr(md5('light-reading-' || hour_point::text), 18, 3) || '-' ||
        substr(md5('light-reading-' || hour_point::text), 21, 12)
        )::uuid AS reading_id,

    -- All generated readings belong to the single smart light above.
    'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa'::uuid AS device_id,

    hour_point AS recorded_at,

    -- JSONB keeps the values together while allowing different device
    -- types to use different measurement fields in a future iteration.
    jsonb_build_object(
            'status',
            CASE
                WHEN EXTRACT(
                        HOUR FROM hour_point AT TIME ZONE 'America/Vancouver'
                     ) BETWEEN 7 AND 22
                    THEN 'ON'
                ELSE 'OFF'
                END,

            'brightnessPercent',
            CASE
                WHEN EXTRACT(
                        HOUR FROM hour_point AT TIME ZONE 'America/Vancouver'
                     ) BETWEEN 7 AND 22
                    THEN 75
                ELSE 0
                END,

            'powerWatts',
            CASE
                WHEN EXTRACT(
                        HOUR FROM hour_point AT TIME ZONE 'America/Vancouver'
                     ) BETWEEN 7 AND 22
                    THEN 9.5
                ELSE 0
                END,

            'energyKWh',
            CASE
                WHEN EXTRACT(
                        HOUR FROM hour_point AT TIME ZONE 'America/Vancouver'
                     ) BETWEEN 7 AND 22
                    THEN 0.0095
                ELSE 0.0000
                END
    ) AS reading_data

FROM generate_series(
             TIMESTAMPTZ '2026-09-18 00:00:00 America/Vancouver',
             TIMESTAMPTZ '2026-10-08 23:00:00 America/Vancouver',
             INTERVAL '1 hour'
     ) AS hour_point

-- Allows the script to be rerun without duplicating readings.
-- Existing readings with the same generated ID are updated.
    ON CONFLICT (reading_id) DO UPDATE SET
    device_id = EXCLUDED.device_id,
                                    recorded_at = EXCLUDED.recorded_at,
                                    reading_data = EXCLUDED.reading_data;

COMMIT;
