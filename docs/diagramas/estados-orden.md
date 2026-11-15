# Estados de la orden de trabajo

```mermaid
stateDiagram-v2
    [*] --> PENDIENTE : se registra la orden
    PENDIENTE --> EN_PROCESO : el técnico empieza
    PENDIENTE --> CANCELADA
    EN_PROCESO --> COMPLETADA : trabajo terminado
    EN_PROCESO --> CANCELADA
    COMPLETADA --> [*]
    CANCELADA --> [*]
```

- `OrdenTrabajoService.cambiarEstado` valida cada cambio.
- Cualquier otra transición, por ejemplo `PENDIENTE → COMPLETADA` o reabrir una orden `COMPLETADA`, lanza `InvalidStateTransitionException` y la API responde **409 Conflict**.
- Una orden completada o cancelada tampoco se puede modificar: responde **422**.
