-- Colores adicionales
INSERT INTO attribute_values (attribute_id, value, created_at, updated_at) VALUES
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'COLOR'), 'Plateado', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'COLOR'), 'Dorado', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'COLOR'), 'Bronce', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'COLOR'), 'Gris', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'COLOR'), 'Beige', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'COLOR'), 'Turquesa', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'COLOR'), 'Coral', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'COLOR'), 'Magenta', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'COLOR'), 'Índigo', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'COLOR'), 'Champán', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'COLOR'), 'Esmeralda', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'COLOR'), 'Rubí', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'COLOR'), 'Zafiro', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'COLOR'), 'Crema', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'COLOR'), 'Transparente', NOW(), NOW());

-- Talla de anillo (escala mexicana, equivale a la escala US/internacional)
INSERT INTO attributes (attribute_type, name, created_at, updated_at) VALUES ('RING_SIZE', 'Talla de anillo', NOW(), NOW());

INSERT INTO attribute_values (attribute_id, value, created_at, updated_at) VALUES
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'RING_SIZE'), '5', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'RING_SIZE'), '6', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'RING_SIZE'), '7', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'RING_SIZE'), '8', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'RING_SIZE'), '9', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'RING_SIZE'), '10', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'RING_SIZE'), '11', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'RING_SIZE'), '12', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'RING_SIZE'), '13', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'RING_SIZE'), '14', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'RING_SIZE'), '15', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'RING_SIZE'), '16', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'RING_SIZE'), '17', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'RING_SIZE'), '18', NOW(), NOW());

-- Largo de cadena
INSERT INTO attributes (attribute_type, name, created_at, updated_at) VALUES ('CHAIN_LENGTH', 'Largo de cadena', NOW(), NOW());

INSERT INTO attribute_values (attribute_id, value, created_at, updated_at) VALUES
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'CHAIN_LENGTH'), '35cm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'CHAIN_LENGTH'), '40cm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'CHAIN_LENGTH'), '45cm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'CHAIN_LENGTH'), '50cm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'CHAIN_LENGTH'), '55cm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'CHAIN_LENGTH'), '60cm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'CHAIN_LENGTH'), '70cm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'CHAIN_LENGTH'), '80cm', NOW(), NOW());

-- Tamaño de brazalete
INSERT INTO attributes (attribute_type, name, created_at, updated_at) VALUES ('BRACELET_SIZE', 'Tamaño de brazalete', NOW(), NOW());

INSERT INTO attribute_values (attribute_id, value, created_at, updated_at) VALUES
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'BRACELET_SIZE'), '14cm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'BRACELET_SIZE'), '16cm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'BRACELET_SIZE'), '17cm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'BRACELET_SIZE'), '18cm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'BRACELET_SIZE'), '19cm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'BRACELET_SIZE'), '20cm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'BRACELET_SIZE'), '21cm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'BRACELET_SIZE'), '22cm', NOW(), NOW());

-- Piedra preciosa
INSERT INTO attributes (attribute_type, name, created_at, updated_at) VALUES ('GEMSTONE', 'Piedra preciosa', NOW(), NOW());

INSERT INTO attribute_values (attribute_id, value, created_at, updated_at) VALUES
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'GEMSTONE'), 'Diamante', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'GEMSTONE'), 'Rubí', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'GEMSTONE'), 'Esmeralda', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'GEMSTONE'), 'Zafiro', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'GEMSTONE'), 'Amatista', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'GEMSTONE'), 'Topacio', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'GEMSTONE'), 'Perla', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'GEMSTONE'), 'Ópalo', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'GEMSTONE'), 'Cuarzo rosa', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'GEMSTONE'), 'Turquesa', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'GEMSTONE'), 'Granate', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'GEMSTONE'), 'Aguamarina', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'GEMSTONE'), 'Circón', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'GEMSTONE'), 'Obsidiana', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'GEMSTONE'), 'Sin piedra', NOW(), NOW());

-- Acabado
INSERT INTO attributes (attribute_type, name, created_at, updated_at) VALUES ('FINISH', 'Acabado', NOW(), NOW());

INSERT INTO attribute_values (attribute_id, value, created_at, updated_at) VALUES
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'FINISH'), 'Pulido brillante', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'FINISH'), 'Satinado', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'FINISH'), 'Mate', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'FINISH'), 'Cepillado', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'FINISH'), 'Envejecido', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'FINISH'), 'Martillado', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'FINISH'), 'Texturizado', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'FINISH'), 'Bañado en oro', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'FINISH'), 'Bañado en rodio', NOW(), NOW());

-- Pureza de plata
INSERT INTO attributes (attribute_type, name, created_at, updated_at) VALUES ('SILVER_PURITY', 'Pureza de plata', NOW(), NOW());

INSERT INTO attribute_values (attribute_id, value, created_at, updated_at) VALUES
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'SILVER_PURITY'), '950', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'SILVER_PURITY'), '925', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'SILVER_PURITY'), '900', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'SILVER_PURITY'), '800', NOW(), NOW());

-- Grosor
INSERT INTO attributes (attribute_type, name, created_at, updated_at) VALUES ('THICKNESS', 'Grosor', NOW(), NOW());

INSERT INTO attribute_values (attribute_id, value, created_at, updated_at) VALUES
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'THICKNESS'), '1mm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'THICKNESS'), '1.5mm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'THICKNESS'), '2mm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'THICKNESS'), '2.5mm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'THICKNESS'), '3mm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'THICKNESS'), '4mm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'THICKNESS'), '5mm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'THICKNESS'), '6mm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'THICKNESS'), '8mm', NOW(), NOW()),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'THICKNESS'), '10mm', NOW(), NOW());
