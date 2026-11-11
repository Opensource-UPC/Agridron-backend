DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'farm' AND column_name = 'image'
    ) THEN
        ALTER TABLE farm ADD COLUMN image VARCHAR(500);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'parcel' AND column_name = 'image'
    ) THEN
        ALTER TABLE parcel ADD COLUMN image VARCHAR(500);
    END IF;
END $$;
