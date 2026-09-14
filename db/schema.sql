-- Esquema de base de datos para el sistema de ordenes en linea

CREATE DATABASE IF NOT EXISTS sistema_ordenes
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sistema_ordenes;

-- Usada por AuthenticationHandler y PermissionHandler:
-- credenciales del usuario y si tiene permisos administrativos.
CREATE TABLE usuarios (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    es_administrador    BOOLEAN      NOT NULL DEFAULT FALSE,
    creado_en           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB;

-- Usada por RateLimitHandler: registra cada intento de login para
-- poder contar cuantos han fallado recientemente desde una misma IP.
-- nombre_usuario se guarda como texto y NO como llave foranea hacia
-- usuarios a proposito: esta tabla debe poder registrar intentos con
-- un usuario que no existe, que son justo los mas relevantes para
-- detectar fuerza bruta. RateLimitHandler cuenta por direccion_ip,
-- no por usuario, asi que esta tabla no depende de que el usuario
-- sea valido. Acoplarla a usuarios mezclaria dos responsabilidades
-- que cambian por razones distintas: autenticacion y deteccion de
-- abuso.
CREATE TABLE intentos_login (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    direccion_ip    VARCHAR(45) NOT NULL,
    nombre_usuario  VARCHAR(50) NOT NULL,
    exitoso         BOOLEAN     NOT NULL,
    intentado_en    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_intentos_ip_fecha (direccion_ip, intentado_en)
) ENGINE = InnoDB;

-- Usada por CacheHandler: guarda la respuesta ya calculada para una
-- solicitud, identificada por una clave unica derivada de sus datos.
CREATE TABLE respuestas_cacheadas (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    clave_solicitud VARCHAR(255) NOT NULL UNIQUE,
    respuesta_json  TEXT         NOT NULL,
    creado_en       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expira_en       TIMESTAMP    NOT NULL,
    INDEX idx_cache_expira (expira_en)
) ENGINE = InnoDB;

-- Datos minimos para poder probar la cadena completa de inmediato.
INSERT INTO usuarios (nombre_usuario, password_hash, es_administrador) VALUES
    ('rostin', SHA2('clave123', 256), FALSE),
    ('admin',  SHA2('admin123', 256), TRUE);
