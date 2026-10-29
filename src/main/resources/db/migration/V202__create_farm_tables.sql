CREATE TABLE farm (
    id         BIGSERIAL PRIMARY KEY,
    owner_id   BIGINT       NOT NULL,
    name       VARCHAR(120) NOT NULL,
    location   VARCHAR(255) NOT NULL,
    department VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL,
    version    BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_farm_owner_id_name UNIQUE (owner_id, name)
);

CREATE INDEX idx_farm_owner_id ON farm (owner_id);

CREATE TABLE farm_supervisor (
    farm_id       BIGINT NOT NULL,
    supervisor_id BIGINT NOT NULL,
    CONSTRAINT pk_farm_supervisor PRIMARY KEY (farm_id, supervisor_id),
    CONSTRAINT fk_farm_supervisor_farm_id
        FOREIGN KEY (farm_id) REFERENCES farm (id) ON DELETE CASCADE
);

CREATE INDEX idx_farm_supervisor_supervisor_id ON farm_supervisor (supervisor_id);
