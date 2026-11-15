package ec.edu.espe.mantenimiento.service;

import ec.edu.espe.mantenimiento.dto.*;
import ec.edu.espe.mantenimiento.entity.Equipo;
import ec.edu.espe.mantenimiento.exception.*;
import ec.edu.espe.mantenimiento.repository.EquipoRepository;
import ec.edu.espe.mantenimiento.repository.MantenimientoRepository;
import ec.edu.espe.mantenimiento.entity.EstadoMantenimiento;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class EquipoService {
    private final EquipoRepository equipos;
    private final MantenimientoRepository mantenimientos;
    public EquipoService(EquipoRepository equipos, MantenimientoRepository mantenimientos) { this.equipos = equipos; this.mantenimientos = mantenimientos; }

    public EquipoResponse crear(EquipoRequest request) {
        if (equipos.existsBySerialIgnoreCase(request.serial().trim())) throw new ConflictException("Ya existe un equipo con ese serial");
        Equipo equipo = new Equipo(request.serial().trim(), request.nombre().trim(), request.ubicacion().trim());
        if (request.activo() != null) equipo.setActivo(request.activo());
        return EquipoResponse.from(equipos.save(equipo));
    }
    @Transactional(readOnly = true)
    public List<EquipoResponse> listar() { return equipos.findAll().stream().map(EquipoResponse::from).toList(); }
    @Transactional(readOnly = true)
    public EquipoResponse obtener(Long id) { return EquipoResponse.from(buscar(id)); }
    public EquipoResponse actualizar(Long id, EquipoRequest request) {
        Equipo e = buscar(id);
        equipos.findBySerialIgnoreCase(request.serial().trim()).filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new ConflictException("Ya existe un equipo con ese serial"); });
        e.setSerial(request.serial().trim()); e.setNombre(request.nombre().trim()); e.setUbicacion(request.ubicacion().trim());
        if (request.activo() != null) e.setActivo(request.activo());
        return EquipoResponse.from(equipos.save(e));
    }
    public void eliminar(Long id) {
        Equipo e = buscar(id);
        if (mantenimientos.existsByEquipoIdAndEstado(id, EstadoMantenimiento.PROGRAMADO))
            throw new ConflictException("No se puede eliminar un equipo con mantenimientos programados");
        try { equipos.delete(e); equipos.flush(); }
        catch (DataIntegrityViolationException ex) { throw new ConflictException("No se puede eliminar un equipo asociado a mantenimientos"); }
    }
    private Equipo buscar(Long id) { return equipos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Equipo " + id + " no encontrado")); }
}
