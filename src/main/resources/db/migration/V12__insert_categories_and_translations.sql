-- Insertar catálogo inicial en inglés
INSERT INTO categories (name) VALUES
    ('Rings'),
    ('Necklaces'),
    ('Bracelets'),
    ('Earrings'),
    ('Brooches'),
    ('Watches'),
    ('Anklets'),
    ('Earcuffs'),
    ('Hoops');

-- Insertar traducciones en español en la tabla translations
-- type = 'CATEGORY', register_id = category_id correspondiente
-- language_id = 1 corresponde a 'es'

INSERT INTO translations (register_id, translation, language_id, type) VALUES
    ((SELECT category_id FROM categories WHERE name = 'Rings'), 'Anillos', 1, 'CATEGORY'),
    ((SELECT category_id FROM categories WHERE name = 'Necklaces'), 'Collares', 1, 'CATEGORY'),
    ((SELECT category_id FROM categories WHERE name = 'Bracelets'), 'Pulseras', 1, 'CATEGORY'),
    ((SELECT category_id FROM categories WHERE name = 'Earrings'), 'Aretes', 1, 'CATEGORY'),
    ((SELECT category_id FROM categories WHERE name = 'Brooches'), 'Broches', 1, 'CATEGORY'),
    ((SELECT category_id FROM categories WHERE name = 'Watches'), 'Relojes', 1, 'CATEGORY'),
    ((SELECT category_id FROM categories WHERE name = 'Anklets'), 'Tobilleras', 1, 'CATEGORY'),
    ((SELECT category_id FROM categories WHERE name = 'Earcuffs'), 'Aretes de presión', 1, 'CATEGORY'),
    ((SELECT category_id FROM categories WHERE name = 'Hoops'), 'Argollas', 1, 'CATEGORY');
