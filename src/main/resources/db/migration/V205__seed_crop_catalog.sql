INSERT INTO crop (name, variety, created_at, updated_at, version) VALUES
    ('Café', 'Catimor', NOW(), NOW(), 0),
    ('Papa', 'Única', NOW(), NOW(), 0),
    ('Arroz', 'IR-43', NOW(), NOW(), 0),
    ('Maíz amarillo', 'Amarillo duro', NOW(), NOW(), 0),
    ('Uva', 'Red Globe', NOW(), NOW(), 0),
    ('Palta', 'Hass', NOW(), NOW(), 0),
    ('Cacao', 'Criollo', NOW(), NOW(), 0),
    ('Caña de azúcar', 'Híbrido', NOW(), NOW(), 0),
    ('Espárrago', 'UC-157', NOW(), NOW(), 0),
    ('Quinua', 'Blanca de Junín', NOW(), NOW(), 0),
    ('Arándano', 'Biloxi', NOW(), NOW(), 0)
ON CONFLICT (name, variety) DO NOTHING;
