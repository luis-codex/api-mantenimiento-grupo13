package ec.edu.espe.mantenimiento;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ec.edu.espe.mantenimiento.service.EquipoService;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MantenimientoApiIntegrationTest {
    @Value("${local.server.port}") int port;
    @Autowired JdbcTemplate jdbc;
    @Autowired EquipoService equipoService;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void limpiarDatos() {
        jdbc.execute("DELETE FROM ordenes_trabajo");
        jdbc.execute("DELETE FROM mantenimientos");
        jdbc.execute("DELETE FROM tecnicos");
        jdbc.execute("DELETE FROM equipos");
    }

    @Test
    void equipoPuedeRegistrarseConsultarseActualizarseYEliminarse() throws Exception {
        HttpResponse<String> created = request("POST", "/api/equipos", """
                {"serial":"EQ-100","nombre":"Compresor","ubicacion":"Planta A"}
                """);
        assertEquals(201, created.statusCode());
        long id = id(created.body());
        assertEquals(200, request("GET", "/api/equipos/" + id, null).statusCode());
        assertEquals(200, request("PUT", "/api/equipos/" + id, """
                {"serial":"EQ-100","nombre":"Compresor renovado","ubicacion":"Planta B","activo":true}
                """).statusCode());
        assertTrue(request("GET", "/api/equipos/" + id, null).body().contains("Compresor renovado"));
        assertEquals(204, request("DELETE", "/api/equipos/" + id, null).statusCode());
        assertEquals(404, request("GET", "/api/equipos/" + id, null).statusCode());
    }

    @Test
    void asignacionOrdenesYReglasDeNegocioSeAplican() throws Exception {
        long equipoId = id(request("POST", "/api/equipos", """
                {"serial":"EQ-200","nombre":"Bomba","ubicacion":"Planta B"}
                """).body());
        long tecnicoId = id(request("POST", "/api/tecnicos", """
                {"nombres":"Ana Pérez","email":"ana@example.edu","especialidad":"Mecánica"}
                """).body());
        LocalDateTime fecha = LocalDateTime.now().plusDays(2).withMinute(15).withSecond(0).withNano(0);
        String maintenanceBody = """ 
                {"equipoId":%d,"tecnicoId":%d,"fechaProgramada":"%s","descripcion":"Revisión preventiva"}
                """.formatted(equipoId, tecnicoId, fecha);
        HttpResponse<String> maintenance = request("POST", "/api/mantenimientos", maintenanceBody);
        assertEquals(201, maintenance.statusCode(), maintenance.body());
        long mantenimientoId = id(maintenance.body());
        assertTrue(request("GET", "/api/mantenimientos/pendientes", null).body().contains("Revisión preventiva"));
        assertEquals(409, request("POST", "/api/mantenimientos", maintenanceBody).statusCode());

        HttpResponse<String> order = request("POST", "/api/ordenes", """
                {"mantenimientoId":%d,"descripcion":"Inspeccionar rodamientos"}
                """.formatted(mantenimientoId));
        assertEquals(201, order.statusCode(), order.body());
        long orderId = id(order.body());
        assertEquals(409, request("PATCH", "/api/ordenes/" + orderId + "/estado", """
                {"estado":"COMPLETADA"}
                """).statusCode());
        assertEquals(200, request("PATCH", "/api/ordenes/" + orderId + "/estado", """
                {"estado":"EN_PROCESO"}
                """).statusCode());
        assertEquals(200, request("PATCH", "/api/ordenes/" + orderId + "/estado", """
                {"estado":"COMPLETADA"}
                """).statusCode());
        assertEquals(409, request("PATCH", "/api/ordenes/" + orderId + "/estado", """
                {"estado":"PENDIENTE"}
                """).statusCode());
        assertEquals(409, request("DELETE", "/api/equipos/" + equipoId, null).statusCode());
        assertEquals(204, request("DELETE", "/api/ordenes/" + orderId, null).statusCode());
    }

    @Test
    void validaFormatoReglasDeFechaYRecursosInexistentes() throws Exception {
        HttpResponse<String> invalid = request("POST", "/api/equipos", """
                {"serial":"","nombre":" ","ubicacion":""}
                """);
        assertEquals(400, invalid.statusCode());
        assertTrue(invalid.body().contains("timestamp"));
        assertTrue(invalid.body().contains("path"));
        assertEquals(404, request("GET", "/api/tecnicos/999999", null).statusCode());

        long equipoId = id(request("POST", "/api/equipos", """
                {"serial":"EQ-300","nombre":"Torno","ubicacion":"Taller"}
                """).body());
        long tecnicoId = id(request("POST", "/api/tecnicos", """
                {"nombres":"Luis Gómez","email":"luis@example.edu","especialidad":"Industrial"}
                """).body());
        String past = LocalDateTime.now().minusDays(1).withNano(0).toString();
        HttpResponse<String> response = request("POST", "/api/mantenimientos", """
                {"equipoId":%d,"tecnicoId":%d,"fechaProgramada":"%s","descripcion":"Mantenimiento pasado"}
                """.formatted(equipoId, tecnicoId, past));
        assertEquals(422, response.statusCode(), response.body());
    }

    @Test
    void actualizacionCorrectaQuedaVisibleEnConsulta() throws Exception {
        long id = id(request("POST", "/api/equipos", """
                {"serial":"EQ-UPDATE","nombre":"Motor","ubicacion":"A"}
                """).body());
        HttpResponse<String> updated = request("PUT", "/api/equipos/" + id, """
                {"serial":"EQ-UPDATE","nombre":"Motor actualizado","ubicacion":"B","activo":true}
                """);
        assertEquals(200, updated.statusCode());
        assertTrue(request("GET", "/api/equipos/" + id, null).body().contains("Motor actualizado"));
    }

    @Test
    void eliminacionCorrectaDevuelveNoContent() throws Exception {
        long id = id(request("POST", "/api/equipos", """
                {"serial":"EQ-DELETE","nombre":"Temporal","ubicacion":"A"}
                """).body());
        assertEquals(204, request("DELETE", "/api/equipos/" + id, null).statusCode());
        assertEquals(404, request("GET", "/api/equipos/" + id, null).statusCode());
    }

    @Test
    void busquedaDeRecursoInexistenteDevuelve404() throws Exception {
        HttpResponse<String> response = request("GET", "/api/tecnicos/999999999", null);
        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Resource Not Found") || response.body().contains("Not Found"));
    }

    @Test
    void violacionDeReglaDeNegocioDevuelve409() throws Exception {
        request("POST", "/api/equipos", """
                {"serial":"EQ-DUPLICADO","nombre":"Equipo","ubicacion":"A"}
                """);
        HttpResponse<String> response = request("POST", "/api/equipos", """
                {"serial":"EQ-DUPLICADO","nombre":"Otro equipo","ubicacion":"B"}
                """);
        assertEquals(409, response.statusCode());
        assertTrue(response.body().contains("serial"));
    }

    @Test
    void registroCorrectoPersisteRegistro() throws Exception {
        HttpResponse<String> response = request("POST", "/api/equipos", """
                {"serial":"EQ-REG","nombre":"Motor","ubicacion":"Taller"}
                """);
        assertEquals(201, response.statusCode());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM equipos WHERE serial = 'EQ-REG'", Integer.class));
    }

    @Test
    void consultasPersonalizadasPorTecnicoEspecialidadYEquipoEstado() throws Exception {
        long equipoId = id(request("POST", "/api/equipos", """
                {"serial":"EQ-QRY","nombre":"Bomba","ubicacion":"Planta"}
                """).body());
        long tecnicoId = id(request("POST", "/api/tecnicos", """
                {"nombres":"Técnica Consulta","email":"consulta@example.edu","especialidad":"Hidráulica"}
                """).body());
        String fecha = LocalDateTime.now().plusDays(3).withMinute(20).withSecond(0).withNano(0).toString();
        long mantenimientoId = id(request("POST", "/api/mantenimientos", """
                {"equipoId":%d,"tecnicoId":%d,"fechaProgramada":"%s","descripcion":"Consulta personalizada"}
                """.formatted(equipoId, tecnicoId, fecha)).body());
        long ordenId = id(request("POST", "/api/ordenes", """
                {"mantenimientoId":%d,"descripcion":"Orden para consulta"}
                """.formatted(mantenimientoId)).body());

        assertTrue(request("GET", "/api/tecnicos?especialidad=hidráulica", null).body().contains("Técnica Consulta"));
        assertTrue(request("GET", "/api/mantenimientos?tecnicoId=" + tecnicoId, null).body().contains("Consulta personalizada"));
        assertTrue(request("GET", "/api/ordenes/equipo/" + equipoId + "?estado=PENDIENTE", null).body().contains("Orden para consulta"));
        assertEquals(200, request("GET", "/api/ordenes/" + ordenId, null).statusCode());
    }

    @Test
    void interceptorAgregaDuracionYRechazaIdentificadorVacio() throws Exception {
        HttpResponse<String> ok = request("GET", "/api/equipos", null);
        assertEquals(200, ok.statusCode());
        assertNotNull(ok.headers().firstValue("X-Response-Time").orElse(null));
        HttpResponse<String> rejected = requestWithHeader("GET", "/api/equipos", null, "X-Client-Id", "");
        assertEquals(400, rejected.statusCode());
    }

    @Test
    void aspectoEstaAplicadoAlServicio() {
        assertTrue(AopUtils.isAopProxy(equipoService));
        assertInstanceOf(Advised.class, equipoService);
        Advised advised = (Advised) equipoService;
        assertTrue(java.util.Arrays.stream(advised.getAdvisors())
                .anyMatch(a -> a.getAdvice().getClass().getName().contains("AspectJAroundAdvice")));
    }

    @Test
    void datosPersistenEnLaBaseRelacionalDePrueba() throws Exception {
        HttpResponse<String> saved = request("POST", "/api/equipos", """
                {"serial":"EQ-PERSIST","nombre":"Compresor","ubicacion":"Sala 2"}
                """);
        assertEquals(201, saved.statusCode());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM equipos WHERE serial = 'EQ-PERSIST'", Integer.class));
        assertTrue(request("GET", "/api/equipos", null).body().contains("EQ-PERSIST"));
    }

    private HttpResponse<String> request(String method, String path, String body) throws Exception {
        return requestWithHeader(method, path, body, null, null);
    }
    private HttpResponse<String> requestWithHeader(String method, String path, String body, String header, String value) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (header != null) builder.header(header, value);
        if (body == null) builder.method(method, HttpRequest.BodyPublishers.noBody());
        else builder.method(method, HttpRequest.BodyPublishers.ofString(body));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
    private long id(String json) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\\"id\\\"\\s*:\\s*(\\d+)").matcher(json);
        assertTrue(matcher.find(), "No se encontró id en: " + json);
        return Long.parseLong(matcher.group(1));
    }
}
