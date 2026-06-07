ALTER TABLE products ALTER COLUMN units_sold SET DEFAULT 0;

UPDATE attributes SET name = 'Color'      WHERE attribute_type = 'COLOR';
UPDATE attributes SET name = 'Talla'      WHERE attribute_type = 'SIZE';
UPDATE attributes SET name = 'Quilataje'  WHERE attribute_type = 'CARAT';
