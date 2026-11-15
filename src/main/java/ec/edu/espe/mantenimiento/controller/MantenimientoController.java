package ec.edu.espe.mantenimiento.controller;

import ec.edu.espe.mantenimiento.dto.*;
import ec.edu.espe.mantenimiento.service.MantenimientoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/mantenimientos")
public class MantenimientoController {
    private final MantenimientoService service;
    public MantenimientoController(MantenimientoService service) { this.service = service; }
    @PostMapping public ResponseEntity<MantenimientoResponse> crear(@Valid @RequestBody MantenimientoRequest body) {
        MantenimientoResponse created = service.crear(body); return ResponseEntity.created(URI.create("/api/mantenimientos/" + created.id())).body(created);
    }
    @GetMapping public List<MantenimientoResponse> listar(@RequestParam(required = false) Long tecnicoId) { return service.listar(tecnicoId); }
    @GetMapping("/pendientes") public List<MantenimientoResponse> pendientes() { return service.listarPendientes(); }
    @GetMapping("/{id}") public MantenimientoResponse obtener(@PathVariable Long id) { return service.obtener(id); }
    @PutMapping("/{id}") public MantenimientoResponse actualizar(@PathVariable Long id, @Valid @RequestBody MantenimientoRequest body) { return service.actualizar(id, body); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id) { service.eliminar(id); return ResponseEntity.noContent().build(); }
}
