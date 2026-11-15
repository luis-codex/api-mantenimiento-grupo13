package ec.edu.espe.mantenimiento.controller;

import ec.edu.espe.mantenimiento.dto.*;
import ec.edu.espe.mantenimiento.entity.EstadoOrden;
import ec.edu.espe.mantenimiento.service.OrdenTrabajoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenTrabajoController {
    private final OrdenTrabajoService service;
    public OrdenTrabajoController(OrdenTrabajoService service) { this.service = service; }
    @PostMapping public ResponseEntity<OrdenTrabajoResponse> crear(@Valid @RequestBody OrdenTrabajoRequest body) {
        OrdenTrabajoResponse created = service.crear(body); return ResponseEntity.created(URI.create("/api/ordenes/" + created.id())).body(created);
    }
    @GetMapping public List<OrdenTrabajoResponse> listar(@RequestParam(required = false) EstadoOrden estado) { return service.listar(estado); }
    @GetMapping("/equipo/{equipoId}")
    public List<OrdenTrabajoResponse> porEquipo(@PathVariable Long equipoId, @RequestParam EstadoOrden estado) {
        return service.listarPorEquipoYEstado(equipoId, estado);
    }
    @GetMapping("/{id}") public OrdenTrabajoResponse obtener(@PathVariable Long id) { return service.obtener(id); }
    @PutMapping("/{id}") public OrdenTrabajoResponse actualizar(@PathVariable Long id, @Valid @RequestBody OrdenTrabajoRequest body) { return service.actualizar(id, body); }
    @PatchMapping("/{id}/estado") public OrdenTrabajoResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoOrdenRequest body) { return service.cambiarEstado(id, body.estado()); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id) { service.eliminar(id); return ResponseEntity.noContent().build(); }
}
