-- ============================================================
-- V1__initial.sql  –  Operaciones Service
-- Esquema e datos iniciales para migración con Flyway/Liquibase
-- ============================================================

-- ─── Tours ───────────────────────────────────────────────────────────────────

CREATE TABLE tour (
    id           BIGINT        PRIMARY KEY AUTO_INCREMENT,
    nombre       VARCHAR(150)  NOT NULL,
    descripcion  TEXT,
    cupo_maximo  INT           NOT NULL CHECK (cupo_maximo > 0),
    precio       DECIMAL(12,2) NOT NULL,
    activo       BOOLEAN       NOT NULL DEFAULT TRUE
);

-- ─── Guias ───────────────────────────────────────────────────────────────────

CREATE TABLE guia (
    id       BIGINT       PRIMARY KEY AUTO_INCREMENT,
    nombre   VARCHAR(150) NOT NULL,
    telefono VARCHAR(30),
    activo   BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ─── Vehiculos ───────────────────────────────────────────────────────────────

CREATE TABLE vehiculo (
    id        BIGINT       PRIMARY KEY AUTO_INCREMENT,
    patente   VARCHAR(20)  NOT NULL UNIQUE,
    modelo    VARCHAR(100),
    capacidad INT,
    activo    BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ─── Bicicletas ──────────────────────────────────────────────────────────────

CREATE TABLE bicicleta (
    id     BIGINT      PRIMARY KEY AUTO_INCREMENT,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    estado VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE'
);

-- ─── Salidas de tour ─────────────────────────────────────────────────────────

CREATE TABLE salida_tour (
    id               BIGINT      PRIMARY KEY AUTO_INCREMENT,
    tour_id          BIGINT      NOT NULL REFERENCES tour(id),
    fecha            DATE        NOT NULL,
    hora_inicio      VARCHAR(10),
    hora_fin         VARCHAR(10),
    cupo_total       INT         NOT NULL,
    cupo_disponible  INT         NOT NULL,
    estado           VARCHAR(30) NOT NULL DEFAULT 'PROGRAMADA',
    guia_id          BIGINT      REFERENCES guia(id),
    vehiculo_id      BIGINT      REFERENCES vehiculo(id)
);

-- ─── Bicicletas asignadas a salidas (relación M:N) ───────────────────────────

CREATE TABLE salida_bicicleta (
    salida_id    BIGINT NOT NULL REFERENCES salida_tour(id),
    bicicleta_id BIGINT NOT NULL REFERENCES bicicleta(id),
    PRIMARY KEY (salida_id, bicicleta_id)
);

-- ─── Reservas de tour ────────────────────────────────────────────────────────

CREATE TABLE reserva_tour (
    id                    BIGINT       PRIMARY KEY AUTO_INCREMENT,
    salida_id             BIGINT       NOT NULL REFERENCES salida_tour(id),
    reserva_hospedaje_id  BIGINT,
    cliente_nombre        VARCHAR(150) NOT NULL,
    pasajeros             INT          NOT NULL CHECK (pasajeros > 0),
    estado                VARCHAR(30)  NOT NULL DEFAULT 'ACTIVA'
);

-- ─── Datos iniciales: Tours disponibles ──────────────────────────────────────

INSERT INTO tour (nombre, descripcion, cupo_maximo, precio, activo) VALUES
('Astroturismo Atacama',       'Observacion del cielo atacameno con telescopios profesionales', 12, 45000, TRUE),
('Valle de la Luna',           'Tour de tarde por miradores y formaciones de sal',              10, 35000, TRUE),
('Trekking Altiplano',         'Caminata por lagunas altoandinas y fauna silvestre',             8, 55000, TRUE),
('Sandboard Medanos',          'Descenso en tabla por las dunas de arena',                      15, 28000, TRUE),
-- Tours NO disponibles (inactivos)
('Geysers del Tatio',          'Visita al campo geotermal al amanecer',                         20, 65000, FALSE),
('Laguna Cejar y Tebinquinche','Flotacion en lagunas de sal y atardecer',                       12, 42000, FALSE);

-- ─── Datos iniciales: Guias ──────────────────────────────────────────────────

INSERT INTO guia (nombre, telefono, activo) VALUES
('Camila Rojas',     '+56911111111', TRUE),
('Andres Cifuentes', '+56922222222', TRUE),
('Lorena Pizarro',   '+56933333333', TRUE);

-- ─── Datos iniciales: Vehiculos ──────────────────────────────────────────────

INSERT INTO vehiculo (patente, modelo, capacidad, activo) VALUES
('ATC-001', 'Van turismo', 12, TRUE),
('ATC-002', 'Minibus',     20, TRUE),
('ATC-003', 'Jeep 4x4',    6,  TRUE);

-- ─── Datos iniciales: Bicicletas ─────────────────────────────────────────────

INSERT INTO bicicleta (codigo, estado) VALUES
('BICI-001', 'DISPONIBLE'),
('BICI-002', 'DISPONIBLE'),
('BICI-003', 'MANTENIMIENTO');

-- ─── Datos iniciales: Salidas (PROGRAMADA + CANCELADA) ───────────────────────
-- Nota: las fechas se calculan relativas; usar NOW() + INTERVAL en producción

INSERT INTO salida_tour (tour_id, fecha, hora_inicio, hora_fin, cupo_total, cupo_disponible, estado, guia_id, vehiculo_id) VALUES
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY),  '21:00', '23:30', 12, 12, 'PROGRAMADA', 1, 1),
(2, DATE_ADD(CURDATE(), INTERVAL 2 DAY),  '16:00', '19:30', 10, 10, 'PROGRAMADA', 2, 3),
(3, DATE_ADD(CURDATE(), INTERVAL 3 DAY),  '07:00', '13:00',  8,  8, 'PROGRAMADA', 3, 3),
(4, DATE_ADD(CURDATE(), INTERVAL 4 DAY),  '10:00', '14:00', 15, 15, 'PROGRAMADA', 1, 2),
(1, DATE_ADD(CURDATE(), INTERVAL 5 DAY),  '21:00', '23:30', 12, 12, 'PROGRAMADA', 2, 1),
(2, DATE_ADD(CURDATE(), INTERVAL 7 DAY),  '16:00', '19:30', 10, 10, 'PROGRAMADA', NULL, NULL),
-- Salidas canceladas (fechas pasadas)
(3, DATE_SUB(CURDATE(), INTERVAL 3 DAY),  '07:00', '13:00',  8,  8, 'CANCELADA',  3, 3),
(4, DATE_SUB(CURDATE(), INTERVAL 1 DAY),  '10:00', '14:00', 15, 15, 'CANCELADA',  1, 2);
