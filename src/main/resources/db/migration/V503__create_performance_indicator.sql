CREATE TABLE performance_indicator (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(120)   NOT NULL,
    value         NUMERIC(14, 2) NOT NULL,
    unit          VARCHAR(30)    NOT NULL,
    period        VARCHAR(7)     NOT NULL,
    calculated_at TIMESTAMPTZ    NOT NULL,
    created_at    TIMESTAMPTZ    NOT NULL,
    updated_at    TIMESTAMPTZ    NOT NULL,
    version       BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_performance_indicator_name_period UNIQUE (name, period),
    CONSTRAINT ck_performance_indicator_value_non_negative CHECK (value >= 0),
    CONSTRAINT ck_performance_indicator_period CHECK (period ~ '^[0-9]{4}-(0[1-9]|1[0-2])$')
);

CREATE INDEX idx_performance_indicator_period ON performance_indicator (period);
