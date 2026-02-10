-- V14__Create_materials_table.sql

CREATE TABLE materials (
    material_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

-- Insertar materiales comunes en joyería (en inglés)
INSERT INTO materials (name) VALUES
    ('Gold'),
    ('Silver'),
    ('Platinum'),
    ('Palladium'),
    ('Titanium'),
    ('Stainless Steel'),
    ('Copper'),
    ('Brass'),
    ('Bronze'),
    ('White Gold'),
    ('Rose Gold'),
    ('Yellow Gold'),
    ('Sterling Silver'),
    ('Rhodium'),
    ('Nickel'),
    ('Tungsten'),
    ('Wood'),
    ('Leather'),
    ('Pearl'),
    ('Diamond'),
    ('Emerald'),
    ('Ruby'),
    ('Sapphire'),
    ('Quartz'),
    ('Amethyst'),
    ('Topaz'),
    ('Opal'),
    ('Turquoise'),
    ('Onyx'),
    ('Jade'),
    ('Gold Plated'); 

-- Insertar traducciones en español en la tabla translations
-- type = 'MATERIAL', register_id = material_id correspondiente
-- language_id = 1 corresponde a 'es'

INSERT INTO translations (register_id, translation, language_id, type) VALUES
    ((SELECT material_id FROM materials WHERE name = 'Gold'), 'Oro', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Silver'), 'Plata', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Platinum'), 'Platino', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Palladium'), 'Paladio', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Titanium'), 'Titanio', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Stainless Steel'), 'Acero inoxidable', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Copper'), 'Cobre', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Brass'), 'Latón', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Bronze'), 'Bronce', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'White Gold'), 'Oro blanco', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Rose Gold'), 'Oro rosa', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Yellow Gold'), 'Oro amarillo', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Sterling Silver'), 'Plata esterlina', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Rhodium'), 'Rodio', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Nickel'), 'Níquel', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Tungsten'), 'Tungsteno', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Wood'), 'Madera', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Leather'), 'Cuero', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Pearl'), 'Perla', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Diamond'), 'Diamante', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Emerald'), 'Esmeralda', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Ruby'), 'Rubí', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Sapphire'), 'Zafiro', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Quartz'), 'Cuarzo', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Amethyst'), 'Amatista', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Topaz'), 'Topacio', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Opal'), 'Ópalo', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Turquoise'), 'Turquesa', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Onyx'), 'Ónix', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Jade'), 'Jade', 1, 'MATERIAL'),
    ((SELECT material_id FROM materials WHERE name = 'Gold Plated'), 'Chapa de oro', 1, 'MATERIAL');
