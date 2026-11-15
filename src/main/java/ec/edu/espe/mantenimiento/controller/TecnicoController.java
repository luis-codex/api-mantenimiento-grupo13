package ec.edu.espe.mantenimiento.controller;

import ec.edu.espe.mantenimiento.dto.*;
import ec.edu.espe.mantenimiento.service.TecnicoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tecnicos")
public class TecnicoController {
    private final TecnicoService service;
    public TecnicoController(TecnicoService service) { this.service = service; }
    @PostMapping public ResponseEntity<TecnicoResponse> crear(@Valid @RequestBody TecnicoRequest body) {
        TecnicoResponse created = service.crear(body); return ResponseEntity.created(URI.create("/api/tecnicos/" + created.id())).body(created);
    }
    @GetMapping public List<TecnicoResponse> listar(@RequestParam(required = false) String especialidad) { return service.listar(especialidad); }
    @GetMapping("/{id}") public TecnicoResponse obtener(@PathVariable Long id) { return service.obtener(id); }
    @PutMapping("/{id}") public TecnicoResponse actualizar(@PathVariable Long id, @Valid @RequestBody TecnicoRequest body) { return service.actualizar(id, body); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id) { service.eliminar(id); return ResponseEntity.noContent().build(); }
}
