-- Inventory & Resource Management: boquillas y drones.
-- nozzle y drone pertenecen al mismo bounded context, así que drone.nozzle_id sí tiene FK física.

CREATE TABLE nozzle (
                        id            BIGSERIAL PRIMARY KEY,
                        type          VARCHAR(60)      NOT NULL,
                        diameter_mm   DOUBLE PRECISION NOT NULL,
                        good_units    INTEGER          NOT NULL DEFAULT 0,
                        worn_units    INTEGER          NOT NULL DEFAULT 0,
                        damaged_units INTEGER          NOT NULL DEFAULT 0,
                        created_at    TIMESTAMPTZ      NOT NULL,
                        updated_at    TIMESTAMPTZ      NOT NULL,
                        version       BIGINT           NOT NULL DEFAULT 0,
                        CONSTRAINT ck_nozzle_diameter_mm_positive CHECK (diameter_mm > 0),
                        CONSTRAINT ck_nozzle_good_units_non_negative CHECK (good_units >= 0),
                        CONSTRAINT ck_nozzle_worn_units_non_negative CHECK (worn_units >= 0),
                        CONSTRAINT ck_nozzle_damaged_units_non_negative CHECK (damaged_units >= 0)
);

CREATE TABLE drone (
                       id                    BIGSERIAL PRIMARY KEY,
                       serial_number         VARCHAR(40)      NOT NULL,
                       model                 VARCHAR(80)      NOT NULL,
                       battery_level         INTEGER          NOT NULL,
                       status                VARCHAR(20)      NOT NULL,
                       nozzle_id             BIGINT           NOT NULL,
                       tank_capacity_liters  DOUBLE PRECISION NOT NULL,
                       flight_hours          INTEGER          NOT NULL DEFAULT 0,
                       last_maintenance_date DATE,
                       next_maintenance_date DATE,
                       urlimg                VARCHAR(500),
                       created_at            TIMESTAMPTZ      NOT NULL,
                       updated_at            TIMESTAMPTZ      NOT NULL,
                       version               BIGINT           NOT NULL DEFAULT 0,
                       CONSTRAINT uk_drone_serial_number UNIQUE (serial_number),
                       CONSTRAINT fk_drone_nozzle_id FOREIGN KEY (nozzle_id) REFERENCES nozzle (id),
                       CONSTRAINT ck_drone_battery_level_range CHECK (battery_level BETWEEN 0 AND 100),
                       CONSTRAINT ck_drone_status CHECK (status IN ('AVAILABLE', 'IN_FLIGHT', 'MAINTENANCE', 'OUT_OF_SERVICE')),
                       CONSTRAINT ck_drone_tank_capacity_liters_positive CHECK (tank_capacity_liters > 0),
                       CONSTRAINT ck_drone_flight_hours_non_negative CHECK (flight_hours >= 0),
                       CONSTRAINT ck_drone_maintenance_dates_order CHECK (
                           last_maintenance_date IS NULL
                               OR next_maintenance_date IS NULL
                               OR next_maintenance_date >= last_maintenance_date)
);

CREATE INDEX idx_drone_nozzle_id ON drone (nozzle_id);
CREATE INDEX idx_drone_status ON drone (status);