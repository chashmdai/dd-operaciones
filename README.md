# Operaciones Service — Atacama Domes

Microservicio de gestión de tours, salidas, recursos (guías, vehículos, bicicletas) y reservas de tour.

## Stack
- Java 21 · Spring Boot 3.5 · almacenamiento en memoria (`ConcurrentHashMap`)
- Puerto: **8082**

## Endpoints

### Tours
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/tours` | Listar tours (activos e inactivos) |
| GET | `/tours/{id}` | Obtener tour |
| POST | `/tours` | Crear tour |
| PUT | `/tours/{id}` | Actualizar tour |

### Salidas
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/salidas` | Listar salidas |
| GET | `/salidas?fecha=YYYY-MM-DD` | Filtrar por fecha |
| GET | `/salidas/{id}` | Obtener salida |
| POST | `/salidas` | Programar salida |
| POST | `/salidas/{id}/cancelar` | Cancelar salida |
| POST | `/salidas/{id}/asignar-guia` | Asignar guía |
| POST | `/salidas/{id}/asignar-vehiculo` | Asignar vehículo |

### Recursos
| Método | Ruta | Descripción |
|---|---|---|
| GET/POST | `/guias` | Listar / crear guías |
| GET | `/guias/{id}/agenda` | Agenda de salidas del guía (orden por fecha) |
| GET/POST | `/vehiculos` | Listar / crear vehículos |
| GET/POST | `/bicicletas` | Listar / crear bicicletas |

### Reservas de tour
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/reservas-tour` | Listar reservas de tour |
| POST | `/reservas-tour` | Reservar cupo en una salida |
| POST | `/reservas-tour/{id}/cancelar` | Cancelar (devuelve cupos) |

## Datos semilla
Al iniciar se cargan automáticamente:
- **6 tours**: 4 activos (Astroturismo, Valle de la Luna, Trekking Altiplano, Sandboard) + 2 inactivos
- **3 guías**, **3 vehículos**, **3 bicicletas**
- **8 salidas**: 6 PROGRAMADA (fechas futuras) + 2 CANCELADA

## Pruebas unitarias

```bash
./mvnw test
```

21 tests en `OperacionesStoreTest` — sin contexto Spring, instancian el store directamente.

## Migración BD (referencia)

`src/main/resources/db/migration/V1__initial.sql` documenta el esquema completo para Flyway/Liquibase.

## Ejecutar en local

```bash
./mvnw spring-boot:run
```

## Docker

```bash
docker build -t dd-operaciones .
```
