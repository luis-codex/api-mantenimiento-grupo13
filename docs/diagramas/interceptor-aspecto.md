# Interceptor HTTP y aspecto de auditoría

```mermaid
sequenceDiagram
    autonumber
    actor Cliente
    participant I as HttpLoggingInterceptor
    participant C as Controller
    participant A as ServiceAuditAspect
    participant S as Service
    participant R as Repository

    Cliente->>I: GET /api/equipos (cabecera X-Client-Id opcional)
    alt X-Client-Id viene vacío
        I-->>Cliente: 400 Bad Request
    else cabecera válida o ausente
        I->>I: preHandle: guarda la hora de inicio
        I->>C: continúa la petición
        C->>A: llama al servicio
        A->>A: registra "llamada a EquipoService.listar"
        A->>S: proceed()
        S->>R: consulta
        R-->>S: datos
        S-->>A: resultado
        A->>A: registra "completada en N ms" (o el error)
        A-->>C: resultado
        C-->>I: respuesta 200
        I->>I: afterCompletion: calcula la duración y registra método, ruta, IP y estado
        I-->>Cliente: 200 OK + cabecera X-Response-Time
    end
```

- **Interceptor (middleware):** actúa a nivel HTTP, antes y después del controller, en todas las rutas `/api/**`.
- **Aspecto (AOP):** actúa a nivel de métodos, con un `@Around` sobre el paquete `service`. Mide y audita sin repetir código en cada servicio.
