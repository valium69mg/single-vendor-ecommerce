-- V21__spanish_only_remove_translations.sql
-- Convierte el catálogo a español únicamente: mueve las traducciones al español
-- a las columnas principales y elimina el subsistema de traducciones.

-- Categorías: reemplazar el nombre en inglés por la traducción en español
UPDATE categories c
SET name = t.translation
FROM translations t
WHERE t.register_id = c.category_id
  AND t.type = 'CATEGORY';

-- Materiales: reemplazar el nombre en inglés por la traducción en español
UPDATE materials m
SET name = t.translation
FROM translations t
WHERE t.register_id = m.material_id
  AND t.type = 'MATERIAL';

-- Atributos: agregar columna de nombre en español y poblarla desde las traducciones
ALTER TABLE attributes ADD COLUMN name VARCHAR(255);

UPDATE attributes a
SET name = t.translation
FROM translations t
WHERE t.register_id = a.attribute_id
  AND t.type = 'ATTRIBUTE';

ALTER TABLE attributes ALTER COLUMN name SET NOT NULL;

-- Valores de atributo de color: reemplazar el valor en inglés por el español
UPDATE attribute_values av
SET value = t.translation
FROM translations t
WHERE t.register_id = av.attribute_value_id
  AND t.type = 'COLOR';

-- Eliminar el subsistema de traducciones
DROP TABLE translations;
DROP TABLE languages;
