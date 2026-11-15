package ec.edu.espe.mantenimiento.service;

import ec.edu.espe.mantenimiento.dto.*;
import ec.edu.espe.mantenimiento.entity.EstadoMantenimiento;
import ec.edu.espe.mantenimiento.entity.Tecnico;
import ec.edu.espe.mantenimiento.exception.*;
import ec.edu.espe.mantenimiento.repository.MantenimientoRepository;
import ec.edu.espe.mantenimiento.repository.TecnicoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class TecnicoService {
    private final TecnicoRepository tecnicos;
    private final MantenimientoRepository mantenimientos;
    public TecnicoService(TecnicoRepository tecnicos, MantenimientoRepository mantenimientos) { this.tecnicos = tecnicos; this.mantenimientos = mantenimientos; }
    public TecnicoResponse crear(TecnicoRequest request) {
        if (tecnicos.existsByEmailIgnoreCase(request.email().trim())) throw new ConflictException("Ya existe un técnico con ese correo");
        Tecnico t = new Tecnico(request.nombres().trim(), request.email().trim().toLowerCase(), request.especialidad().trim());
        if (request.activo() != null) t.setActivo(request.activo());
        return TecnicoResponse.from(tecnicos.save(t));
    }
    @Transactional(readOnly = true)
    public List<TecnicoResponse> listar(String especialidad) {
        List<Tecnico> encontrados = especialidad == null || especialidad.isBlank()
                ? tecnicos.findAll() : tecnicos.buscarActivosPorEspecialidad(especialidad.trim());
        return encontrados.stream().map(TecnicoResponse::from).toList();
    }
    @Transactional(readOnly = true)
    public TecnicoResponse obtener(Long id) { return TecnicoResponse.from(buscar(id)); }
    public TecnicoResponse actualizar(Long id, TecnicoRequest request) {
        Tecnico t = buscar(id);
        tecnicos.findByEmailIgnoreCase(request.email().trim()).filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new ConflictException("Ya existe un técnico con ese correo"); });
        t.setNombres(request.nombres().trim()); t.setEmail(request.email().trim().toLowerCase()); t.setEspecialidad(request.especialidad().trim());
        if (request.activo() != null) t.setActivo(request.activo());
        return TecnicoResponse.from(tecnicos.save(t));
    }
    public void eliminar(Long id) {
        Tecnico t = buscar(id);
        if (mantenimientos.existsByTecnicoIdAndEstado(id, EstadoMantenimiento.PROGRAMADO))
            throw new ConflictException("No se puede eliminar un técnico con mantenimientos programados");
        try { tecnicos.delete(t); tecnicos.flush(); }
        catch (DataIntegrityViolationException ex) { throw new ConflictException("No se puede eliminar un técnico asociado a mantenimientos"); }
    }
    private Tecnico buscar(Long id) { return tecnicos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Técnico " + id + " no encontrado")); }
}
