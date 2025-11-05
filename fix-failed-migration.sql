-- Fix failed Flyway migration
-- This script updates the flyway_schema_history table to mark version 9 as successful

USE reading_tracker;

-- Update the failed migration to success
UPDATE flyway_schema_history 
SET success = 1 
WHERE version = '9' AND success = 0;

-- Verify the update
SELECT * FROM flyway_schema_history WHERE version = '9';

