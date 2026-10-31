-- mission_id y parcel_id referencian a otros bounded contexts (Flight y Field): solo se guarda el id, sin FK física.
CREATE TABLE mission_report (
    id             BIGSERIAL PRIMARY KEY,
    mission_id     BIGINT         NOT NULL,
    parcel_id      BIGINT         NOT NULL,
    type           VARCHAR(30)    NOT NULL,
    treated_area   NUMERIC(12, 2) NOT NULL,
    applied_volume NUMERIC(12, 2) NOT NULL,
    observations   TEXT           NOT NULL DEFAULT '',
    generated_at   TIMESTAMPTZ    NOT NULL,
    created_at     TIMESTAMPTZ    NOT NULL,
    updated_at     TIMESTAMPTZ    NOT NULL,
    version        BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_mission_report_mission_id_type UNIQUE (mission_id, type),
    CONSTRAINT ck_mission_report_type CHECK (type IN ('MISSION_SUMMARY', 'PHYTOSANITARY')),
    CONSTRAINT ck_mission_report_treated_area_non_negative CHECK (treated_area >= 0),
    CONSTRAINT ck_mission_report_applied_volume_non_negative CHECK (applied_volume >= 0)
);

CREATE INDEX idx_mission_report_parcel_id ON mission_report (parcel_id);
CREATE INDEX idx_mission_report_generated_at ON mission_report (generated_at);

CREATE TABLE operational_metric (
    id                BIGSERIAL PRIMARY KEY,
    mission_report_id BIGINT         NOT NULL,
    name              VARCHAR(120)   NOT NULL,
    value             NUMERIC(14, 3) NOT NULL,
    unit              VARCHAR(30)    NOT NULL,
    created_at        TIMESTAMPTZ    NOT NULL,
    updated_at        TIMESTAMPTZ    NOT NULL,
    version           BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_operational_metric_mission_report_id_name UNIQUE (mission_report_id, name),
    CONSTRAINT fk_operational_metric_mission_report_id FOREIGN KEY (mission_report_id)
        REFERENCES mission_report (id) ON DELETE CASCADE
);

CREATE INDEX idx_operational_metric_mission_report_id ON operational_metric (mission_report_id);
