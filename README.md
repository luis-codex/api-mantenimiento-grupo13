# API de Gestión de Mantenimiento – Grupo 13

Proyecto grupal de la Unidad 1 de **Programación Avanzada**. Es una API REST con Spring Boot para administrar **equipos, técnicos, mantenimientos y órdenes de trabajo**. La información se guarda en PostgreSQL.

## Funcionalidades

- Registrar equipos y técnicos.
- Programar mantenimientos y asignar un técnico a cada uno.
- Registrar órdenes de trabajo y cambiar su estado.
- Consultar los mantenimientos pendientes.
- Validar reglas de negocio con respuestas HTTP claras (400, 404, 409, 422).

## Tecnologías

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje |
| Spring Boot 4.1 (Web MVC, Data JPA, Validation, AspectJ) | Framework de la API |
| PostgreSQL 16 | Base de datos relacional |
| H2 | Base de datos en memoria, solo para las pruebas |
| JUnit 5 y Mockito | Pruebas unitarias y de integración |
| GitHub Actions | Pipeline de integración continua |
| Docker Compose | Levantar la base de datos y la API |

## Arquitectura en capas

```
src/main/java/ec/edu/espe/mantenimiento
├── controller   → Recibe las peticiones HTTP y devuelve respuestas (sin lógica de negocio)
├── service      → Reglas de negocio y transacciones
├── repository   → Acceso a datos con Spring Data JPA y consultas JPQL
├── entity       → Entidades JPA y enumeraciones de estado
├── dto          → Objetos de entrada y salida con validaciones
├── exception    → Excepciones personalizadas y manejador global
├── middleware   → Interceptor HTTP y su configuración
├── aspect       → Aspecto de auditoría (AOP)
└── async        → Notificaciones asíncronas y su grupo de hilos
```

Flujo de una petición: `Cliente → Interceptor → Controller → Service (+ Aspecto) → Repository → PostgreSQL`.

## Modelo de datos

Son 4 entidades relacionadas, con claves primarias, claves foráneas y restricciones de unicidad:

- **Equipo** (`equipos`): serial único, nombre, ubicación y si está activo.
- **Técnico** (`tecnicos`): nombres, email único, especialidad y si está activo.
- **Mantenimiento** (`mantenimientos`): pertenece a un equipo y a un técnico; tiene fecha programada y estado.
- **Orden de trabajo** (`ordenes_trabajo`): pertenece a un mantenimiento; tiene estado y fechas de creación y actualización.

Relaciones: un equipo tiene muchos mantenimientos, un técnico atiende muchos mantenimientos y un mantenimiento tiene muchas órdenes. Detalle completo en [docs/descripcion-entidades.md](docs/descripcion-entidades.md).

## Endpoints

Base: `http://localhost:8080`

| Recurso | GET | POST | PUT / PATCH | DELETE |
|---|---|---|---|---|
| `/api/equipos` | Listar, por id | Registrar | Actualizar | Eliminar |
| `/api/tecnicos` | Listar, por id, por especialidad (`?especialidad=`) | Registrar | Actualizar | Eliminar |
| `/api/mantenimientos` | Listar, por id, por técnico (`?tecnicoId=`), pendientes (`/pendientes`) | Programar | Actualizar | Eliminar |
| `/api/ordenes` | Listar, por id, por estado (`?estado=`), por equipo y estado (`/equipo/{equipoId}`) | Registrar | Actualizar, cambiar estado (`PATCH /{id}/estado`) | Eliminar |

Tabla completa con códigos de respuesta en [docs/endpoints.md](docs/endpoints.md).

### Consultas personalizadas (Spring Data / JPQL)

- `TecnicoRepository.buscarActivosPorEspecialidad`: técnicos activos de una especialidad.
- `MantenimientoRepository.buscarPorTecnico`: mantenimientos de un técnico.
- `OrdenTrabajoRepository.buscarPorEquipoYEstado`: órdenes de un equipo en un estado.
- `MantenimientoRepository.tecnicoTieneMantenimientoEnDia`: verifica si el técnico ya está ocupado en esa hora.
- Pendientes: mantenimientos `PROGRAMADO` con fecha futura (consulta derivada del nombre del método).

## Reglas de negocio

1. El serial del equipo y el email del técnico no se pueden repetir (409).
2. No se puede programar un mantenimiento en una fecha pasada (422).
3. No se puede programar mantenimiento a un equipo inactivo ni asignar un técnico inactivo (422).
4. Un técnico no puede tener dos mantenimientos en la misma hora (409).
5. Las órdenes solo pasan de `PENDIENTE → EN_PROCESO/CANCELADA` y de `EN_PROCESO → COMPLETADA/CANCELADA`; una orden completada o cancelada no se reabre (409).
6. No se puede eliminar un equipo o técnico con mantenimientos programados, ni un mantenimiento con órdenes activas (409).

Detalle en [docs/reglas-negocio.md](docs/reglas-negocio.md).

## Manejo de excepciones

Excepciones personalizadas: `ResourceNotFoundException` (404), `ConflictException` (409), `InvalidStateTransitionException` (409) y `BusinessRuleException` (422). El manejador global `ApiExceptionHandler` devuelve siempre el mismo formato:

```json
{ "timestamp": "...", "status": 404, "error": "Not Found", "message": "Equipo 99 no encontrado", "path": "/api/equipos/99" }
```

## Middleware, AOP y procesamiento asíncrono

- **Interceptor HTTP** (`HttpLoggingInterceptor`): en las rutas `/api/**` registra método, ruta, IP, estado y duración; agrega la cabecera `X-Response-Time` y rechaza con 400 una cabecera `X-Client-Id` vacía.
- **Aspecto** (`ServiceAuditAspect`): un `@Around` sobre la capa de servicios mide cada llamada, registra cuándo termina y registra los errores.
- **Bloqueante vs. no bloqueante**: enviar la notificación al técnico tarda y bloquearía la petición. `NotificacionService` usa `@Async` con un grupo de hilos propio, así que la API responde sin esperar el envío.

Explicación en [docs/middleware-aop.md](docs/middleware-aop.md) y [docs/bloqueante-vs-no-bloqueante.md](docs/bloqueante-vs-no-bloqueante.md).

## Cómo ejecutar

**Requisitos:** Java 21 y Docker.

### Opción 1: todo con Docker

```bash
docker compose up --build
```

Levanta PostgreSQL y la API en `http://localhost:8080`.

### Opción 2: base en Docker y API local

```bash
docker compose up -d db
./mvnw spring-boot:run
```

En Windows usar `mvnw.cmd spring-boot:run`. La conexión se configura con las variables `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`; por defecto usa `jdbc:postgresql://localhost:5432/mantenimiento` con usuario y clave `mantenimiento`.

## Pruebas

```bash
./mvnw verify
```

Las pruebas usan H2 en memoria (perfil `test`), así que no necesitan PostgreSQL. Cubren registro, consulta, actualización y eliminación correctos, recursos inexistentes, reglas de negocio, validaciones, el interceptor, el aspecto y la persistencia.

## Integración continua

El pipeline [`.github/workflows/ci.yml`](.github/workflows/ci.yml) se ejecuta en cada push y pull request:

1. Obtiene el código del repositorio.
2. Configura Java 21.
3. Compila el proyecto.
4. Ejecuta las pruebas.
5. Publica los reportes de pruebas e informa si la construcción fue exitosa o falló.

El resultado se ve en la pestaña **Actions** del repositorio.

## Integrantes – Grupo 13

- Marcos Steban Bajaña Ruiz
- Rosario
- Luis
- Sebas

La participación de cada integrante se puede ver en el historial de commits del repositorio.
