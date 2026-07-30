ALTER TABLE vehicle
    ADD COLUMN IF NOT EXISTS image_available BOOLEAN DEFAULT FALSE;

UPDATE vehicle
SET image_available = FALSE
WHERE image_available IS NULL;

ALTER TABLE vehicle
    ALTER COLUMN image_available SET DEFAULT FALSE;

ALTER TABLE vehicle
    ALTER COLUMN image_available SET NOT NULL;
