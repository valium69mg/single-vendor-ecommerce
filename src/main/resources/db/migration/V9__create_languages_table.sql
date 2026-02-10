-- V9__Create_languages_table.sql
CREATE TABLE languages (
    language_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(12) NOT NULL
);

-- Insertar idiomas iniciales
INSERT INTO languages (name) VALUES ('es'), ('en');
