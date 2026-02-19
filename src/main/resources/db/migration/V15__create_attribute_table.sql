CREATE TABLE attributes (
    attribute_id BIGSERIAL PRIMARY KEY,
    attribute_type VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO attributes (attribute_type) VALUES ('COLOR');
INSERT INTO attributes (attribute_type) VALUES ('SIZE');
INSERT INTO attributes (attribute_type) VALUES ('CARAT');

INSERT INTO translations (register_id, translation, language_id, type) VALUES
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'COLOR'), 'COLOR', 1, 'ATTRIBUTE'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'SIZE'), 'TALLA', 1, 'ATTRIBUTE'),
    ((SELECT attribute_id FROM attributes WHERE attribute_type = 'CARAT'), 'QUILATAJE', 1, 'ATTRIBUTE');
