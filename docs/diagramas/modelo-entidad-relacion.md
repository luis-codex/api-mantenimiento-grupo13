# Modelo entidad-relación

```mermaid
erDiagram
    EQUIPOS ||--o{ MANTENIMIENTOS : "tiene"
    TECNICOS ||--o{ MANTENIMIENTOS : "atiende"
    MANTENIMIENTOS ||--o{ ORDENES_TRABAJO : "genera"

    EQUIPOS {
        BIGINT id PK
        VARCHAR serial UK "obligatorio"
        VARCHAR nombre "obligatorio"
        VARCHAR ubicacion "obligatorio"
        BOOLEAN activo "inicial true"
    }

    TECNICOS {
        BIGINT id PK
        VARCHAR nombres "obligatorio"
        VARCHAR email UK "obligatorio"
        VARCHAR especialidad "obligatorio"
        BOOLEAN activo "inicial true"
    }

    MANTENIMIENTOS {
        BIGINT id PK
        BIGINT equipo_id FK "a equipos"
        BIGINT tecnico_id FK "a tecnicos"
        TIMESTAMP fecha_programada "obligatorio"
        VARCHAR descripcion "obligatorio"
        VARCHAR estado "PROGRAMADO, COMPLETADO, CANCELADO"
    }

    ORDENES_TRABAJO {
        BIGINT id PK
        BIGINT mantenimiento_id FK "a mantenimientos"
        VARCHAR descripcion "obligatorio"
        VARCHAR estado "PENDIENTE, EN_PROCESO, COMPLETADA, CANCELADA"
        TIMESTAMP creada_en "obligatorio"
        TIMESTAMP actualizada_en "obligatorio"
    }
```

| Relación | Tipo | Clave foránea |
|---|---|---|
| Un equipo tiene muchos mantenimientos | 1 : N | `mantenimientos.equipo_id` |
| Un técnico atiende muchos mantenimientos | 1 : N | `mantenimientos.tecnico_id` |
| Un mantenimiento genera muchas órdenes de trabajo | 1 : N | `ordenes_trabajo.mantenimiento_id` |

Restricciones de integridad: claves primarias autogeneradas, `serial` y `email` únicos, y columnas obligatorias `NOT NULL`.
