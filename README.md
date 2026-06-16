# Operaciones Service - Atacama Domes

Microservicio basico para gestionar tours, salidas, guias, vehiculos, bicicletas y reservas de tour del MVP de Atacama Domes.

Esta version sigue enfoque KISS: no usa base de datos, no usa JPA y guarda datos en memoria mientras la aplicacion esta corriendo.

## Stack

- Java 21
- Spring Boot 3.5.x
- Spring Web
- Spring Boot Actuator
- Maven

## Puerto

```text
http://localhost:8082
```

Cuando se consume desde el gateway:

```text
http://localhost:8080/api/operaciones
```

## Datos iniciales

Al iniciar carga datos de ejemplo:

- Tours: Astroturismo, Valle de la Luna
- Guia: Camila Rojas
- Vehiculo: ATC-001
- Bicicleta: BICI-001
- Salidas programadas de ejemplo

## Endpoints

### Tours

```http
GET    /tours
GET    /tours/{id}
POST   /tours
PUT    /tours/{id}
```

### Salidas

```http
GET    /salidas
GET    /salidas/{id}
GET    /salidas?fecha=YYYY-MM-DD
POST   /salidas
POST   /salidas/{id}/cancelar
POST   /salidas/{id}/asignar-guia
POST   /salidas/{id}/asignar-vehiculo
POST   /salidas/{id}/asignar-bicicleta
```

### Guias

```http
GET    /guias
POST   /guias
GET    /guias/{id}/agenda
```

### Vehiculos

```http
GET    /vehiculos
POST   /vehiculos
PUT    /vehiculos/{id}
```

### Bicicletas

```http
GET    /bicicletas
POST   /bicicletas
PUT    /bicicletas/{id}
```

### Reservas de tour

```http
GET    /reservas-tour
GET    /reservas-tour/{id}
GET    /reservas-tour?reservaHospedajeId=123
POST   /reservas-tour
POST   /reservas-tour/{id}/cancelar
```

### Health

```http
GET    /actuator/health
```

## Ejecutar

Desde esta carpeta:

```powershell
.\mvnw.cmd spring-boot:run
```

O usando el jar:

```powershell
.\mvnw.cmd -DskipTests package
java -jar target\operaciones-service-0.0.1-SNAPSHOT.jar
```

## Pruebas

```powershell
.\mvnw.cmd test
```

## Ejemplos con PowerShell

Listar tours:

```powershell
Invoke-RestMethod http://localhost:8082/tours
```

Crear tour:

```powershell
$tour = Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8082/tours `
  -ContentType "application/json" `
  -Body '{"nombre":"Tour Demo","descripcion":"Tour creado desde PowerShell","cupoMaximo":8,"precio":45000,"activo":true}'

$tour
```

Crear guia:

```powershell
$guia = Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8082/guias `
  -ContentType "application/json" `
  -Body '{"nombre":"Guia Demo","telefono":"+56911112222","activo":true}'
```

Crear salida:

```powershell
$salida = Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8082/salidas `
  -ContentType "application/json" `
  -Body "{`"tourId`":$($tour.id),`"fecha`":`"2026-07-10`",`"horaInicio`":`"09:00`",`"horaFin`":`"13:00`",`"cupoTotal`":8,`"estado`":`"PROGRAMADA`",`"guiaId`":$($guia.id)}"

$salida
```

Reservar cupos en una salida:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8082/reservas-tour `
  -ContentType "application/json" `
  -Body "{`"salidaId`":$($salida.id),`"reservaHospedajeId`":1,`"clienteNombre`":`"Cliente Demo`",`"pasajeros`":2}"
```

## Reglas simples implementadas

- Una salida queda con cupo disponible igual al cupo total si no se indica otro valor.
- Solo se puede reservar una salida en estado `PROGRAMADA`.
- La reserva de tour descuenta cupos disponibles.
- Cancelar una reserva de tour devuelve cupos.
- Cancelar una salida la marca como `CANCELADA`.
- Las asignaciones de guia, vehiculo y bicicleta validan que existan.

## Notas

- Los datos se pierden al apagar el servicio.
- Este servicio no usa base de datos real todavia.
- Para la demostracion automatizada, usar el script raiz `crud-microservices-demo.ps1`.
