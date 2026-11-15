# Descripción de entidades

Las entidades JPA del proyecto se almacenan en PostgreSQL. `id` es generado
por la base de datos con identidad y las columnas marcadas como obligatorias no
aceptan `NULL`.

## Equipo (`equipos`)

| Atributo | Tipo Java / SQL | Restricciones |
|---|---|---|
| `id` | `Long` / `BIGINT` | PK, generado automáticamente |
| `serial` | `String` / `VARCHAR(80)` | Obligatorio, único |
| `nombre` | `String` / `VARCHAR(140)` | Obligatorio |
| `ubicacion` | `String` / `VARCHAR(180)` | Obligatorio |
| `activo` | `boolean` / `BOOLEAN` | Obligatorio, valor inicial `true` |

Un equipo tiene cero o muchos mantenimientos (`1:N`).

## Técnico (`tecnicos`)

| Atributo | Tipo Java / SQL | Restricciones |
|---|---|---|
| `id` | `Long` / `BIGINT` | PK, generado automáticamente |
| `nombres` | `String` / `VARCHAR(140)` | Obligatorio |
| `email` | `String` / `VARCHAR(180)` | Obligatorio, único |
| `especialidad` | `String` / `VARCHAR(120)` | Obligatorio |
| `activo` | `boolean` / `BOOLEAN` | Obligatorio, valor inicial `true` |

Un técnico puede atender cero o muchos mantenimientos (`1:N`).

## Mantenimiento (`mantenimientos`)

| Atributo | Tipo Java / SQL | Restricciones |
|---|---|---|
| `id` | `Long` / `BIGINT` | PK, generado automáticamente |
| `equipo` | `Equipo` / `BIGINT equipo_id` | Obligatorio, FK a `equipos` |
| `tecnico` | `Tecnico` / `BIGINT tecnico_id` | Obligatorio, FK a `tecnicos` |
| `fechaProgramada` | `LocalDateTime` / `TIMESTAMP` | Obligatorio |
| `descripcion` | `String` / `VARCHAR(500)` | Obligatorio |
| `estado` | `EstadoMantenimiento` / `VARCHAR(20)` | Obligatorio, inicial `PROGRAMADO` |

Cada mantenimiento pertenece a un equipo y a un técnico (`N:1`) y puede tener
cero o muchas órdenes de trabajo (`1:N`).

## Orden de trabajo (`ordenes_trabajo`)

| Atributo | Tipo Java / SQL | Restricciones |
|---|---|---|
| `id` | `Long` / `BIGINT` | PK, generado automáticamente |
| `mantenimiento` | `Mantenimiento` / `BIGINT mantenimiento_id` | Obligatorio, FK |
| `descripcion` | `String` / `VARCHAR(500)` | Obligatorio |
| `estado` | `EstadoOrden` / `VARCHAR(20)` | Obligatorio, inicial `PENDIENTE` |
| `creadaEn` | `LocalDateTime` / `TIMESTAMP` | Obligatorio, fecha de creación |
| `actualizadaEn` | `LocalDateTime` / `TIMESTAMP` | Obligatorio, se actualiza al cambiar estado |

Cada orden de trabajo pertenece a un único mantenimiento (`N:1`).

## Enumeraciones

- `EstadoMantenimiento`: estado del ciclo de mantenimiento, inicialmente
  `PROGRAMADO`.
- `EstadoOrden`: estado de la orden, inicialmente `PENDIENTE`; sus transiciones
  son validadas por el servicio.
