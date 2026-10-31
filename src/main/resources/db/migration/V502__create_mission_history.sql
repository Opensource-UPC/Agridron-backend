-- mission_id y parcel_id referencian a otros bounded contexts: solo se guarda el id, sin FK física.
CREATE TABLE mission_history (
    id           BIGSERIAL PRIMARY KEY,
    mission_id   BIGINT      NOT NULL,
    parcel_id    BIGINT      NOT NULL,
    final_status VARCHAR(20) NOT NULL,
    completed_at TIMESTAMPTZ NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL,
    version      BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT uk_mission_history_mission_id UNIQUE (mission_id),
    CONSTRAINT ck_mission_history_final_status CHECK (final_status IN ('COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_mission_history_parcel_id ON mission_history (parcel_id);
CREATE INDEX idx_mission_history_completed_at ON mission_history (completed_at);
