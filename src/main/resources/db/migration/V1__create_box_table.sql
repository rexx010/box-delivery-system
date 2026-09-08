CREATE TABLE box (
                     id                BIGSERIAL PRIMARY KEY,
                     txref             VARCHAR(20)    NOT NULL UNIQUE,
                     weight_limit      NUMERIC(6, 2)  NOT NULL,
                     battery_capacity  INTEGER        NOT NULL,
                     state             VARCHAR(20)    NOT NULL
);