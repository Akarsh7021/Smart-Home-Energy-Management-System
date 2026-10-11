-- ============================================================
-- DEMO LOGIN CREDENTIALS (local testing only)
-- Demo login password for all three seeded users: password
-- password_hash stores the BCrypt hash for the demo password.
-- These are local prototype accounts only; do not use in production.
-- ============================================================

-- 02_insert_mock_data.sql
-- Smart Home Energy Management System - Stage 1
-- Adds sample users, one simulated smart light, and 25 days of hourly readings.
-- This script inserts data only; it does not create or alter tables.
-- Spring Session tables are not included.

BEGIN;

-- DEMO LOGIN: use password as the password for each seeded account.
-- The same BCrypt hash is assigned to all three demo accounts.
-- Three accounts let the prototype test the Homeowner role,
-- a permitted Family Member, and a Family Member without permissions.
INSERT INTO users
(user_id, name, email, password_hash, role,
 can_control_devices, can_view_energy)
VALUES
    ('11111111-1111-4111-8111-111111111111',
     'Alex Homeowner', 'homeowner@example.com',
     '$2a$10$8MVpMQ4qkcgQgYR0dCGrWuWZh/GdUUnYfXIdkSCM53evlxQ3QxWAO',
     'HOMEOWNER', TRUE, TRUE),
--Password: password

    ('22222222-2222-4222-8222-222222222222',
     'Sam Family Member', 'family@example.com',
     '$2a$10$8MVpMQ4qkcgQgYR0dCGrWuWZh/GdUUnYfXIdkSCM53evlxQ3QxWAO',
     'FAMILY_MEMBER', TRUE, TRUE),
--Password: password

    ('33333333-3333-4333-8333-333333333333',
     'Taylor Restricted Member', 'restricted@example.com',
     '$2a$10$8MVpMQ4qkcgQgYR0dCGrWuWZh/GdUUnYfXIdkSCM53evlxQ3QxWAO',
     'FAMILY_MEMBER', FALSE, FALSE)
    --Password: password

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

-- Replace all existing mock readings for this demo light before inserting
-- the new dataset. This removes stale readings outside the new date range too.
-- The transaction means the delete and insert succeed together or roll back.
DELETE FROM device_readings
WHERE device_id = 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa'::uuid;

-- Generate hourly mock readings from Sep 18 through Oct 12, 2026.
-- This covers 25 calendar days: 25 * 24 = 600 hourly readings.
--
-- The values are simulated, not measured from physical hardware. The schedule
-- follows common usage (morning and evening), but includes daytime/late-night
-- use and occasional hours when the light is off, so the graph is not uniform.
-- hashtext() provides repeatable pseudo-random variation when this script reruns.
--
-- powerWatts is instantaneous/representative power for that hour.
-- energyKWh is energy consumed during that individual one-hour interval:
-- power in watts / 1000 * 1 hour. It is not a cumulative meter reading.
--
-- generate_series() creates one timestamp per hour, including both endpoints.
INSERT INTO device_readings
(reading_id, device_id, recorded_at, reading_data)
SELECT
    -- Keep IDs stable per timestamp so rerunning this script updates readings
    -- rather than creating duplicate rows.
    (
        substr(md5('light-reading-' || hour_point::text), 1, 8) || '-' ||
        substr(md5('light-reading-' || hour_point::text), 9, 4) || '-' ||
        '4' || substr(md5('light-reading-' || hour_point::text), 14, 3) || '-' ||
        '8' || substr(md5('light-reading-' || hour_point::text), 18, 3) || '-' ||
        substr(md5('light-reading-' || hour_point::text), 21, 12)
        )::uuid AS reading_id,

    'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa'::uuid AS device_id,
    hour_point AS recorded_at,

    jsonb_build_object(
            'status',
            CASE
                -- Mostly used in the morning and evening, with some variation.
                WHEN local_hour BETWEEN 7 AND 10
                    AND daytime_roll >= 15 THEN 'ON'
                WHEN local_hour BETWEEN 17 AND 22
                    AND evening_roll >= 10 THEN 'ON'
                -- Occasional daytime use.
                WHEN local_hour BETWEEN 11 AND 16
                    AND daytime_roll < 22 THEN 'ON'
                -- Rare late-night use.
                WHEN (local_hour BETWEEN 0 AND 6 OR local_hour = 23)
                    AND late_roll < 7 THEN 'ON'
                ELSE 'OFF'
                END,

            'brightnessPercent',
            CASE
                WHEN (
                    (local_hour BETWEEN 7 AND 10 AND daytime_roll >= 15)
                        OR (local_hour BETWEEN 17 AND 22 AND evening_roll >= 10)
                        OR (local_hour BETWEEN 11 AND 16 AND daytime_roll < 22)
                        OR ((local_hour BETWEEN 0 AND 6 OR local_hour = 23) AND late_roll < 7)
                    )
                    THEN 40 + mod(abs(hashtext('brightness-' || hour_point::text)::bigint), 61)
                ELSE 0
                END,

            'powerWatts',
            CASE
                WHEN (
                    (local_hour BETWEEN 7 AND 10 AND daytime_roll >= 15)
                        OR (local_hour BETWEEN 17 AND 22 AND evening_roll >= 10)
                        OR (local_hour BETWEEN 11 AND 16 AND daytime_roll < 22)
                        OR ((local_hour BETWEEN 0 AND 6 OR local_hour = 23) AND late_roll < 7)
                    )
                    THEN round((5.0 + mod(abs(hashtext('power-' || hour_point::text)::bigint), 41) / 10.0)::numeric, 1)
                ELSE 0.0
                END,

            'energyKWh',
            CASE
                WHEN (
                    (local_hour BETWEEN 7 AND 10 AND daytime_roll >= 15)
                        OR (local_hour BETWEEN 17 AND 22 AND evening_roll >= 10)
                        OR (local_hour BETWEEN 11 AND 16 AND daytime_roll < 22)
                        OR ((local_hour BETWEEN 0 AND 6 OR local_hour = 23) AND late_roll < 7)
                    )
                    THEN round(
                        (5.0 + mod(abs(hashtext('power-' || hour_point::text)::bigint), 41) / 10.0)
                            / 1000.0, 5
                         )
                ELSE 0.00000
                END
    ) AS reading_data
FROM (
         SELECT
             hour_point,
             EXTRACT(HOUR FROM hour_point AT TIME ZONE 'America/Vancouver')::int AS local_hour,
             mod(abs(hashtext('day-' || (hour_point AT TIME ZONE 'America/Vancouver')::date::text)::bigint), 100) AS daytime_roll,
             mod(abs(hashtext('evening-' || hour_point::text)::bigint), 100) AS evening_roll,
             mod(abs(hashtext('late-' || hour_point::text)::bigint), 100) AS late_roll
         FROM generate_series(
                      TIMESTAMPTZ '2026-09-18 00:00:00 America/Vancouver',
                      TIMESTAMPTZ '2026-10-12 23:00:00 America/Vancouver',
                      INTERVAL '1 hour'
              ) AS hour_point
     ) AS hourly_mock

;

COMMIT;
