CREATE TABLE products (
    product_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(200) NOT NULL,
    short_description VARCHAR(500),
    long_description TEXT,
    status VARCHAR(20) NOT NULL,
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    brand_id BIGINT,
    category_id BIGINT,
    CONSTRAINT fk_brand FOREIGN KEY (brand_id) REFERENCES brands(brand_id),
    CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES categories(category_id)
);


CREATE TABLE product_materials (
    product_material_id BIGSERIAL PRIMARY KEY,
    product_id UUID NOT NULL,
    material_id BIGINT NOT NULL,
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_material FOREIGN KEY (material_id) REFERENCES materials(material_id),
    CONSTRAINT uq_product_material UNIQUE (product_id, material_id)
);

CREATE TABLE product_variants (
    product_variant_id BIGSERIAL PRIMARY KEY,
    product_id UUID NOT NULL,
    sku VARCHAR(100) NOT NULL UNIQUE,
    price NUMERIC(10,2) NOT NULL,
    discount_price NUMERIC(10,2),
    stock INTEGER NOT NULL,
    weight_grams INTEGER,
    CONSTRAINT fk_product_variant_product FOREIGN KEY (product_id) REFERENCES products(product_id)
);


CREATE TABLE attribute_values (
    attribute_value_id BIGSERIAL PRIMARY KEY,
    attribute_id BIGINT NOT NULL,
    value VARCHAR(255) NOT NULL,
    CONSTRAINT fk_attribute FOREIGN KEY (attribute_id) REFERENCES attributes(attribute_id),
    CONSTRAINT uq_attribute_value UNIQUE (attribute_id, value)
);

CREATE TABLE product_variant_attributes (
    product_variant_attribute_id BIGSERIAL PRIMARY KEY,
    product_variant_id BIGINT NOT NULL,
    attribute_value_id BIGINT NOT NULL,
    CONSTRAINT fk_variant FOREIGN KEY (product_variant_id) REFERENCES product_variants(product_variant_id),
    CONSTRAINT fk_attribute_value FOREIGN KEY (attribute_value_id) REFERENCES attribute_values(attribute_value_id),
    CONSTRAINT uq_variant_attribute UNIQUE (product_variant_id, attribute_value_id)
);

INSERT INTO attribute_values (attribute_id, value) VALUES
    ((SELECT attribute_id FROM attributes WHERE attribute_type='COLOR'), 'Red'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='COLOR'), 'Blue'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='COLOR'), 'Green'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='COLOR'), 'Yellow'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='COLOR'), 'Black'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='COLOR'), 'White'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='COLOR'), 'Pink'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='COLOR'), 'Purple'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='COLOR'), 'Orange'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='COLOR'), 'Brown');

INSERT INTO translations (register_id, translation, language_id, type) VALUES
    ((SELECT attribute_value_id FROM attribute_values WHERE value='Red'), 'Rojo', 1, 'COLOR'),
    ((SELECT attribute_value_id FROM attribute_values WHERE value='Blue'), 'Azul', 1, 'COLOR'),
    ((SELECT attribute_value_id FROM attribute_values WHERE value='Green'), 'Verde', 1, 'COLOR'),
    ((SELECT attribute_value_id FROM attribute_values WHERE value='Yellow'), 'Amarillo', 1, 'COLOR'),
    ((SELECT attribute_value_id FROM attribute_values WHERE value='Black'), 'Negro', 1, 'COLOR'),
    ((SELECT attribute_value_id FROM attribute_values WHERE value='White'), 'Blanco', 1, 'COLOR'),
    ((SELECT attribute_value_id FROM attribute_values WHERE value='Pink'), 'Rosa', 1, 'COLOR'),
    ((SELECT attribute_value_id FROM attribute_values WHERE value='Purple'), 'Morado', 1, 'COLOR'),
    ((SELECT attribute_value_id FROM attribute_values WHERE value='Orange'), 'Naranja', 1, 'COLOR'),
    ((SELECT attribute_value_id FROM attribute_values WHERE value='Brown'), 'Marrón', 1, 'COLOR');
    
INSERT INTO attribute_values (attribute_id, value) VALUES
    ((SELECT attribute_id FROM attributes WHERE attribute_type='SIZE'), 'XS'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='SIZE'), 'S'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='SIZE'), 'M'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='SIZE'), 'L'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='SIZE'), 'XL'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='SIZE'), 'XXL'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='SIZE'), '28'), 
    ((SELECT attribute_id FROM attributes WHERE attribute_type='SIZE'), '30'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='SIZE'), '32'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='SIZE'), '34'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='SIZE'), '36'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='SIZE'), '38'); 
    
INSERT INTO attribute_values (attribute_id, value) VALUES
    ((SELECT attribute_id FROM attributes WHERE attribute_type='CARAT'), '9k'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='CARAT'), '10k'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='CARAT'), '14k'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='CARAT'), '18k'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='CARAT'), '22k'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type='CARAT'), '24k');