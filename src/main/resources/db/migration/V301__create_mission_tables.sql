-- Flight Operations: mission aggregate and its child entities.
-- parcel_id, drone_id, agrochemical_id and operator_id reference other bounded contexts,
-- so they are stored without physical foreign keys (conventions §5.4).

CREATE TABLE mission (
    id              BIGSERIAL PRIMARY KEY,
    requested_date  DATE             NOT NULL,
    parcel_id       BIGINT           NOT NULL,
    agrochemical_id BIGINT,
    drone_id        BIGINT,
    operator_id     BIGINT,
    flight_height   DOUBLE PRECISION,
    flight_speed    DOUBLE PRECISION,
    spray_rate      DOUBLE PRECISION,
    overlap         DOUBLE PRECISION,
    status          VARCHAR(20)      NOT NULL,
    created_at      TIMESTAMPTZ      NOT NULL,
    updated_at      TIMESTAMPTZ      NOT NULL,
    version         BIGINT           NOT NULL DEFAULT 0,
    CONSTRAINT ck_mission_status CHECK (status IN (
        'PLANNED', 'AUTHORIZED', 'IN_PROGRESS', 'PAUSED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_mission_flight_parameters CHECK (
        (flight_height IS NULL AND flight_speed IS NULL AND spray_rate IS NULL AND overlap IS NULL)
        OR (flight_height > 0 AND flight_speed > 0 AND spray_rate > 0
            AND overlap >= 0 AND overlap < 100)
    ),
    CONSTRAINT ck_mission_authorized_resources CHECK (
        status IN ('PLANNED', 'CANCELLED')
        OR (drone_id IS NOT NULL AND agrochemical_id IS NOT NULL
            AND operator_id IS NOT NULL AND flight_height IS NOT NULL)
    )
);

CREATE INDEX idx_mission_parcel_id ON mission (parcel_id);
CREATE INDEX idx_mission_status ON mission (status);
CREATE INDEX idx_mission_drone_id ON mission (drone_id);
CREATE INDEX idx_mission_operator_id ON mission (operator_id);

CREATE TABLE mission_result (
    id             BIGSERIAL PRIMARY KEY,
    mission_id     BIGINT           NOT NULL,
    treated_area   DOUBLE PRECISION NOT NULL,
    applied_volume DOUBLE PRECISION NOT NULL,
    observations   VARCHAR(1000),
    CONSTRAINT uk_mission_result_mission_id UNIQUE (mission_id),
    CONSTRAINT fk_mission_result_mission_id
        FOREIGN KEY (mission_id) REFERENCES mission (id) ON DELETE CASCADE,
    CONSTRAINT ck_mission_result_non_negative CHECK (treated_area >= 0 AND applied_volume >= 0)
);

CREATE TABLE incident (
    id          BIGSERIAL PRIMARY KEY,
    mission_id  BIGINT        NOT NULL,
    type        VARCHAR(50)   NOT NULL,
    description VARCHAR(1000) NOT NULL,
    occurred_at TIMESTAMPTZ   NOT NULL,
    CONSTRAINT fk_incident_mission_id
        FOREIGN KEY (mission_id) REFERENCES mission (id) ON DELETE CASCADE
);

CREATE INDEX idx_incident_mission_id ON incident (mission_id);

CREATE TABLE operation_status (
    id          BIGSERIAL PRIMARY KEY,
    mission_id  BIGINT           NOT NULL,
    latitude    DOUBLE PRECISION NOT NULL,
    longitude   DOUBLE PRECISION NOT NULL,
    state       VARCHAR(20)      NOT NULL,
    recorded_at TIMESTAMPTZ      NOT NULL,
    CONSTRAINT uk_operation_status_mission_id UNIQUE (mission_id),
    CONSTRAINT fk_operation_status_mission_id
        FOREIGN KEY (mission_id) REFERENCES mission (id) ON DELETE CASCADE,
    CONSTRAINT ck_operation_status_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_operation_status_longitude CHECK (longitude BETWEEN -180 AND 180),
    CONSTRAINT ck_operation_status_state CHECK (state IN (
        'IDLE', 'TAKEOFF', 'IN_FLIGHT', 'SPRAYING', 'RETURNING', 'LANDED'))
);
