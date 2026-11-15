# Arquitectura en capas

```mermaid
flowchart TB
    Cliente["Cliente HTTP<br/>(Postman / navegador)"]

    subgraph API["API Spring Boot"]
        direction TB
        Interceptor["middleware<br/>HttpLoggingInterceptor"]
        Controller["controller<br/>EquipoController · TecnicoController<br/>MantenimientoController · OrdenTrabajoController"]
        DTO["dto<br/>Request / Response con validaciones"]
        Service["service<br/>EquipoService · TecnicoService<br/>MantenimientoService · OrdenTrabajoService"]
        Aspect["aspect<br/>ServiceAuditAspect"]
        Async["async<br/>NotificacionService (@Async)"]
        Repository["repository<br/>Spring Data JPA + consultas JPQL"]
        Entity["entity<br/>Equipo · Tecnico · Mantenimiento · OrdenTrabajo"]
        Exception["exception<br/>ApiExceptionHandler + excepciones personalizadas"]
    end

    DB[("PostgreSQL")]

    Cliente -->|petición| Interceptor --> Controller
    Controller <--> DTO
    Controller --> Service
    Aspect -. audita .-> Service
    Service -->|notifica en otro hilo| Async
    Service --> Repository --> Entity
    Repository --> DB
    Service -. lanza excepciones .-> Exception
    Exception -->|respuesta de error JSON| Cliente
```

- El **controller** solo recibe la petición y devuelve la respuesta; la lógica de negocio está en el **service**.
- El **interceptor** actúa antes y después de cada petición a `/api/**`.
- El **aspecto** envuelve todos los métodos de la capa de servicios.
- El **manejador global** convierte las excepciones en respuestas HTTP con el mismo formato.
