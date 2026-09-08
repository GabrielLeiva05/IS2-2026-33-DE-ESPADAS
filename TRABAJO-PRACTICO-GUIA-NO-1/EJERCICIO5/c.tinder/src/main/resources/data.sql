-- Seed data for Tinder-like app
-- This file runs automatically on startup because spring.sql.init.mode=always is enabled.

-- Zonas
INSERT INTO zona (id, nombre, descripcion) VALUES
    ('zone-001', 'Centro', 'Zona céntrica de la ciudad'),
    ('zone-002', 'Norte', 'Barrio residencial del norte'),
    ('zone-003', 'Sur', 'Comuna del sur con mucho tránsito')
ON CONFLICT (id) DO NOTHING;

-- Fotos
INSERT INTO foto (id, nombre, mime, contenido) VALUES
    ('foto-001', 'perro1.jpg', 'image/jpeg', E'\\x89504e470d0a1a0a'),
    ('foto-002', 'gato1.jpg', 'image/jpeg', E'\\x89504e470d0a1a0a'),
    ('foto-003', 'loro1.jpg', 'image/jpeg', E'\\x89504e470d0a1a0a'),
    ('foto-004', 'conejo1.jpg', 'image/jpeg', E'\\x89504e470d0a1a0a')
ON CONFLICT (id) DO NOTHING;

-- Usuarios
-- La contraseña de todos los usuarios de prueba es: 123456
INSERT INTO usuario (id, nombre, apellido, mail, clave, zona_id, alta, baja, foto_id) VALUES
    ('user-001', 'Ana', 'Pérez', 'ana@tinder.com', '$2a$10$LpYmUgoA0EhGtIRMgiJP0um41eCXFzb2OEe1qYowiN/SG5CuoOJni', 'zone-001', NOW(), NULL, 'foto-001'),
    ('user-002', 'Lucas', 'Gómez', 'lucas@tinder.com', '$2a$10$LpYmUgoA0EhGtIRMgiJP0um41eCXFzb2OEe1qYowiN/SG5CuoOJni', 'zone-002', NOW(), NULL, 'foto-002'),
    ('user-003', 'Sofía', 'Ramirez', 'sofia@tinder.com', '$2a$10$LpYmUgoA0EhGtIRMgiJP0um41eCXFzb2OEe1qYowiN/SG5CuoOJni', 'zone-003', NOW(), NULL, 'foto-003')
ON CONFLICT (id) DO NOTHING;

-- Mascotas
INSERT INTO mascota (id, nombre, alta, baja, usuario_id, sexo, tipo, foto_id) VALUES
    ('pet-001', 'Firulais', NOW(), NULL, 'user-001', 'MACHO', 'PERRO', 'foto-001'),
    ('pet-002', 'Michi', NOW(), NULL, 'user-002', 'HEMBRA', 'GATO', 'foto-002'),
    ('pet-003', 'Luna', NOW(), NULL, 'user-003', 'HEMBRA', 'TORTUGA', 'foto-003'),
    ('pet-004', 'Tito', NOW(), NULL, 'user-001', 'MACHO', 'CONEJO', 'foto-004')
ON CONFLICT (id) DO NOTHING;

-- Votos
INSERT INTO voto (id, fecha, respuesta, mascota1_id, mascota2_id) VALUES
    ('vote-001', NOW(), NOW(), 'pet-001', 'pet-002'),
    ('vote-002', NOW(), NOW(), 'pet-002', 'pet-003'),
    ('vote-003', NOW(), NULL, 'pet-003', 'pet-001')
ON CONFLICT (id) DO NOTHING;
