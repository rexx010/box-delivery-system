CREATE TABLE item (
                      id      BIGSERIAL PRIMARY KEY,
                      name    VARCHAR(255)   NOT NULL,
                      weight  NUMERIC(6, 2)  NOT NULL,
                      code    VARCHAR(255)   NOT NULL,
                      box_id  BIGINT         NOT NULL REFERENCES box (id)
);

CREATE INDEX idx_item_box_id ON item (box_id);