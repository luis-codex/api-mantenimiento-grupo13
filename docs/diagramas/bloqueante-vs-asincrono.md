# Bloqueante vs. asíncrono: notificación al técnico

Al programar un mantenimiento se notifica al técnico asignado. El envío tarda unos 2 segundos (simulado con `Thread.sleep`).

## Si fuera bloqueante

```mermaid
sequenceDiagram
    actor Cliente
    participant H as Hilo de la petición
    participant N as Envío de notificación

    Cliente->>H: POST /api/mantenimientos
    H->>H: valida y guarda el mantenimiento
    H->>N: envía la notificación
    Note over H,N: el hilo queda esperando ~2 s
    N-->>H: enviada
    H-->>Cliente: 201 Created (después de ~2 s)
```

## Como está implementado (asíncrono con @Async)

```mermaid
sequenceDiagram
    actor Cliente
    participant H as Hilo de la petición
    participant P as Grupo de hilos "notificacion-"
    participant N as NotificacionService

    Cliente->>H: POST /api/mantenimientos
    H->>H: valida y guarda el mantenimiento
    H->>P: notificarAsignacion() con @Async
    H-->>Cliente: 201 Created de inmediato
    P->>N: ejecuta en otro hilo
    Note over P,N: el envío tarda ~2 s sin bloquear la API
    N-->>P: CompletableFuture completado
```

El grupo de hilos `notificacionesExecutor` (en `AsyncConfig`) tiene 2 hilos base, máximo 4 y una cola de 20 tareas, para no consumir recursos sin control.
