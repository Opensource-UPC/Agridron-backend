CREATE TABLE fumigation_area (
    id            BIGSERIAL PRIMARY KEY,
    parcel_id     BIGINT          NOT NULL,
    geometry      geometry(Polygon, 4326) NOT NULL,
    area_hectares NUMERIC(12, 4)  NOT NULL,
    created_at    TIMESTAMPTZ     NOT NULL,
    updated_at    TIMESTAMPTZ     NOT NULL,
    version       BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT fk_fumigation_area_parcel_id
        FOREIGN KEY (parcel_id) REFERENCES parcel (id) ON DELETE CASCADE,
    CONSTRAINT ck_fumigation_area_positive CHECK (area_hectares > 0)
);

CREATE INDEX idx_fumigation_area_parcel_id ON fumigation_area (parcel_id);
CREATE INDEX idx_fumigation_area_geometry ON fumigation_area USING GIST (geometry);
