-- V10__Create_translations_table.sql
CREATE TABLE translations (
    translation_id BIGSERIAL PRIMARY KEY,
    register_id INTEGER NOT NULL,
    translation VARCHAR(255) NOT NULL,
    language_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    CONSTRAINT fk_translation_language FOREIGN KEY (language_id) REFERENCES languages(language_id)
);

-- Índices para optimizar búsquedas por tipo y por idioma
CREATE INDEX idx_translations_type ON translations(type);
CREATE INDEX idx_translations_language ON translations(language_id);
