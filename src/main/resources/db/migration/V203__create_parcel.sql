CREATE TABLE parcel (
    id                 BIGSERIAL PRIMARY KEY,
    farm_id            BIGINT        NOT NULL,
    crop_id            BIGINT,
    name               VARCHAR(120)  NOT NULL,
    geometry           geometry(Polygon, 4326) NOT NULL,
    area_hectares      NUMERIC(12, 4) NOT NULL,
    status             VARCHAR(20)   NOT NULL,
    phenological_stage VARCHAR(20),
    created_at         TIMESTAMPTZ   NOT NULL,
    updated_at         TIMESTAMPTZ   NOT NULL,
    version            BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT uk_parcel_farm_id_name UNIQUE (farm_id, name),
    CONSTRAINT fk_parcel_farm_id FOREIGN KEY (farm_id) REFERENCES farm (id),
    CONSTRAINT fk_parcel_crop_id FOREIGN KEY (crop_id) REFERENCES crop (id),
    CONSTRAINT ck_parcel_area_positive CHECK (area_hectares > 0),
    CONSTRAINT ck_parcel_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_parcel_phenological_stage CHECK (
        phenological_stage IS NULL
        OR phenological_stage IN (
            'GERMINATION', 'SEEDLING', 'VEGETATIVE', 'FLOWERING',
            'FRUITING', 'MATURATION', 'HARVEST')
    ),
    CONSTRAINT ck_parcel_crop_stage CHECK (
        (crop_id IS NULL AND phenological_stage IS NULL)
        OR (crop_id IS NOT NULL AND phenological_stage IS NOT NULL)
    )
);

CREATE INDEX idx_parcel_farm_id ON parcel (farm_id);
CREATE INDEX idx_parcel_crop_id ON parcel (crop_id);
CREATE INDEX idx_parcel_status ON parcel (status);
CREATE INDEX idx_parcel_geometry ON parcel USING GIST (geometry);
