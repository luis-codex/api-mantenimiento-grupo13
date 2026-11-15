package ec.edu.espe.mantenimiento.controller;

import ec.edu.espe.mantenimiento.dto.*;
import ec.edu.espe.mantenimiento.service.EquipoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/equipos")
public class EquipoController {
    private final EquipoService service;
    public EquipoController(EquipoService service) { this.service = service; }
    @PostMapping public ResponseEntity<EquipoResponse> crear(@Valid @RequestBody EquipoRequest body) {
        EquipoResponse created = service.crear(body); return ResponseEntity.created(URI.create("/api/equipos/" + created.id())).body(created);
    }
    @GetMapping public List<EquipoResponse> listar() { return service.listar(); }
    @GetMapping("/{id}") public EquipoResponse obtener(@PathVariable Long id) { return service.obtener(id); }
    @PutMapping("/{id}") public EquipoResponse actualizar(@PathVariable Long id, @Valid @RequestBody EquipoRequest body) { return service.actualizar(id, body); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id) { service.eliminar(id); return ResponseEntity.noContent().build(); }
}
