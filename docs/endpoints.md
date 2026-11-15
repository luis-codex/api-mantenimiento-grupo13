# Endpoints REST

Base local: `http://localhost:8080`. Los cuerpos JSON se validan con Jakarta Bean Validation; los errores usan `{timestamp,status,error,message,path}`.

| Método | Ruta | Descripción | Códigos principales |
|---|---|---|---|
| GET | `/api/equipos` | Lista equipos | 200 |
| GET | `/api/equipos/{id}` | Consulta equipo | 200, 404 |
| POST | `/api/equipos` | Registra equipo | 201, 400, 409 |
| PUT | `/api/equipos/{id}` | Actualiza equipo | 200, 400, 404, 409 |
| DELETE | `/api/equipos/{id}` | Elimina equipo sin mantenimientos activos ni referencias | 204, 404, 409 |
| GET | `/api/tecnicos` | Lista técnicos | 200 |
| GET | `/api/tecnicos?especialidad={texto}` (URL-encodea caracteres no ASCII; ej. `Mec%C3%A1nica`) | Busca técnicos activos por especialidad (JPQL) | 200 |
| GET | `/api/tecnicos/{id}` | Consulta técnico | 200, 404 |
| POST | `/api/tecnicos` | Registra técnico | 201, 400, 409 |
| PUT | `/api/tecnicos/{id}` | Actualiza técnico | 200, 400, 404, 409 |
| DELETE | `/api/tecnicos/{id}` | Elimina técnico sin mantenimientos activos ni referencias | 204, 404, 409 |
| GET | `/api/mantenimientos` | Lista mantenimientos | 200 |
| GET | `/api/mantenimientos?tecnicoId={id}` | Busca mantenimientos del técnico (JPQL) | 200 |
| GET | `/api/mantenimientos/pendientes` | Lista mantenimientos programados futuros | 200 |
| GET | `/api/mantenimientos/{id}` | Consulta mantenimiento | 200, 404 |
| POST | `/api/mantenimientos` | Programa mantenimiento y notifica asignación en segundo plano | 201, 400, 404, 409, 422 |
| PUT | `/api/mantenimientos/{id}` | Actualiza mantenimiento programado | 200, 400, 404, 409, 422 |
| DELETE | `/api/mantenimientos/{id}` | Elimina mantenimiento sin órdenes activas | 204, 404, 409 |
| GET | `/api/ordenes` | Lista órdenes | 200 |
| GET | `/api/ordenes?estado={estado}` | Filtra órdenes por estado | 200, 400 |
| GET | `/api/ordenes/equipo/{equipoId}?estado={estado}` | Busca órdenes por equipo y estado (JPQL) | 200, 400 |
| GET | `/api/ordenes/{id}` | Consulta orden | 200, 404 |
| POST | `/api/ordenes` | Registra orden de trabajo | 201, 400, 404, 422 |
| PUT | `/api/ordenes/{id}` | Actualiza orden abierta | 200, 400, 404, 422 |
| PATCH | `/api/ordenes/{id}/estado` | Transiciona estado de orden | 200, 400, 404, 409 |
| DELETE | `/api/ordenes/{id}` | Elimina orden | 204, 404 |

Estados de orden: `PENDIENTE`, `EN_PROCESO`, `COMPLETADA`, `CANCELADA`. Transiciones: `PENDIENTE → EN_PROCESO/CANCELADA`; `EN_PROCESO → COMPLETADA/CANCELADA`. Las órdenes completadas o canceladas son terminales.
