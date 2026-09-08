INSERT INTO box (txref, weight_limit, battery_capacity, state)
VALUES ('BOX00000000000001', 500, 80, 'IDLE');

INSERT INTO box (txref, weight_limit, battery_capacity, state)
VALUES ('BOX00000000000002', 500, 15, 'IDLE');

INSERT INTO box (txref, weight_limit, battery_capacity, state)
VALUES ('BOX00000000000003', 500, 90, 'DELIVERING');

INSERT INTO box (txref, weight_limit, battery_capacity, state)
VALUES ('BOX00000000000004', 500, 70, 'LOADED');

INSERT INTO item (name, weight, code, box_id)
SELECT 'sample-widget', 120.50, 'WIDGET_001', id FROM box WHERE txref = 'BOX00000000000004';

INSERT INTO item (name, weight, code, box_id)
SELECT 'spare_part-2', 75.00, 'PART_002', id FROM box WHERE txref = 'BOX00000000000004';