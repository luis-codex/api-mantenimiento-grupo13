# Reglas de negocio

| Regla | Aplicación | Excepción | HTTP |
|---|---|---|---:|
| Serial de equipo único (sin distinguir mayúsculas) | `EquipoService.crear`, `EquipoService.actualizar`; restricción única en `Equipo` | `ConflictException` | 409 |
| Correo de técnico único (sin distinguir mayúsculas) | `TecnicoService.crear`, `TecnicoService.actualizar`; restricción única en `Tecnico` | `ConflictException` | 409 |
| No programar en el pasado | `MantenimientoService.validarAsignacion` | `BusinessRuleException` | 422 |
| No asignar equipo inactivo | `MantenimientoService.validarAsignacion` | `BusinessRuleException` | 422 |
| No asignar técnico inactivo | `MantenimientoService.validarAsignacion` | `BusinessRuleException` | 422 |
| Un técnico no puede atender dos mantenimientos dentro de la misma hora | `MantenimientoService.validarAsignacion` y `MantenimientoRepository.tecnicoTieneMantenimientoEnDia` (JPQL) | `ConflictException` | 409 |
| No abrir una orden sobre mantenimiento cancelado | `OrdenTrabajoService.crear`, `OrdenTrabajoService.actualizar` | `BusinessRuleException` | 422 |
| Solo se permiten `PENDIENTE → EN_PROCESO/CANCELADA` y `EN_PROCESO → COMPLETADA/CANCELADA`; no reabrir estados terminales | `OrdenTrabajoService.cambiarEstado` | `InvalidStateTransitionException` | 409 |
| No modificar orden completada o cancelada | `OrdenTrabajoService.actualizar` | `BusinessRuleException` | 422 |
| No eliminar equipo/técnico con mantenimientos programados ni equipo con referencias históricas | `EquipoService.eliminar`, `TecnicoService.eliminar` | `ConflictException` | 409 |
| No eliminar mantenimiento con órdenes pendientes o en proceso | `MantenimientoService.eliminar` | `ConflictException` | 409 |
| Campos de entrada deben cumplir restricciones de DTO (`@NotBlank`, `@Email`, `@Size`, `@NotNull`) | DTOs request + `@Valid` en controllers | Respuesta de validación | 400 |
| Recurso inexistente | Métodos `buscar` de los servicios | `ResourceNotFoundException` | 404 |

Las consultas JPQL adicionales son `MantenimientoRepository.buscarPorTecnico`, `OrdenTrabajoRepository.buscarPorEquipoYEstado` y `TecnicoRepository.buscarActivosPorEspecialidad`; se exponen como GET filtrados.
