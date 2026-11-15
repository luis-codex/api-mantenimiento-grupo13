package ec.edu.espe.mantenimiento.service;

import ec.edu.espe.mantenimiento.async.NotificacionService;
import ec.edu.espe.mantenimiento.dto.MantenimientoRequest;
import ec.edu.espe.mantenimiento.dto.MantenimientoResponse;
import ec.edu.espe.mantenimiento.entity.*;
import ec.edu.espe.mantenimiento.exception.*;
import ec.edu.espe.mantenimiento.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class MantenimientoService {
    private final MantenimientoRepository mantenimientos;
    private final EquipoRepository equipos;
    private final TecnicoRepository tecnicos;
    private final OrdenTrabajoRepository ordenes;
    private final NotificacionService notificacionService;
    public MantenimientoService(MantenimientoRepository mantenimientos, EquipoRepository equipos,
            TecnicoRepository tecnicos, OrdenTrabajoRepository ordenes, NotificacionService notificacionService) {
        this.mantenimientos = mantenimientos; this.equipos = equipos; this.tecnicos = tecnicos;
        this.ordenes = ordenes; this.notificacionService = notificacionService;
    }
    public MantenimientoResponse crear(MantenimientoRequest request) {
        Equipo equipo = buscarEquipo(request.equipoId());
        Tecnico tecnico = buscarTecnico(request.tecnicoId());
        validarAsignacion(equipo, tecnico, request.fechaProgramada(), null);
        Mantenimiento guardado = mantenimientos.save(new Mantenimiento(equipo, tecnico, request.fechaProgramada(), request.descripcion().trim()));
        notificacionService.notificarAsignacion(tecnico.getId(), guardado.getId());
        return MantenimientoResponse.from(guardado);
    }
    @Transactional(readOnly = true)
    public List<MantenimientoResponse> listar(Long tecnicoId) {
        List<Mantenimiento> encontrados = tecnicoId == null ? mantenimientos.findAll() : mantenimientos.buscarPorTecnico(tecnicoId);
        return encontrados.stream().map(MantenimientoResponse::from).toList();
    }
    @Transactional(readOnly = true)
    public List<MantenimientoResponse> listarPendientes() {
        return mantenimientos.findByEstadoAndFechaProgramadaGreaterThanEqualOrderByFechaProgramadaAsc(EstadoMantenimiento.PROGRAMADO, LocalDateTime.now())
                .stream().map(MantenimientoResponse::from).toList();
    }
    @Transactional(readOnly = true)
    public MantenimientoResponse obtener(Long id) { return MantenimientoResponse.from(buscar(id)); }
    public MantenimientoResponse actualizar(Long id, MantenimientoRequest request) {
        Mantenimiento m = buscar(id);
        if (m.getEstado() != EstadoMantenimiento.PROGRAMADO) throw new BusinessRuleException("Solo se puede modificar un mantenimiento programado");
        Equipo equipo = buscarEquipo(request.equipoId());
        Tecnico tecnico = buscarTecnico(request.tecnicoId());
        Long tecnicoAnterior = m.getTecnico().getId();
        validarAsignacion(equipo, tecnico, request.fechaProgramada(), id);
        m.setEquipo(equipo); m.setTecnico(tecnico); m.setFechaProgramada(request.fechaProgramada()); m.setDescripcion(request.descripcion().trim());
        Mantenimiento guardado = mantenimientos.save(m);
        if (!Objects.equals(tecnicoAnterior, tecnico.getId())) notificacionService.notificarAsignacion(tecnico.getId(), guardado.getId());
        return MantenimientoResponse.from(guardado);
    }
    public void eliminar(Long id) {
        Mantenimiento m = buscar(id);
        if (ordenes.existsByMantenimientoIdAndEstadoIn(id, List.of(EstadoOrden.PENDIENTE, EstadoOrden.EN_PROCESO)))
            throw new ConflictException("No se puede eliminar un mantenimiento con órdenes activas");
        mantenimientos.delete(m);
    }
    private void validarAsignacion(Equipo equipo, Tecnico tecnico, LocalDateTime fecha, Long excluirId) {
        if (fecha.isBefore(LocalDateTime.now())) throw new BusinessRuleException("No se puede programar un mantenimiento en el pasado");
        if (!equipo.isActivo()) throw new BusinessRuleException("No se puede programar mantenimiento para un equipo inactivo");
        if (!tecnico.isActivo()) throw new BusinessRuleException("No se puede asignar un técnico inactivo");
        LocalDateTime inicioHora = fecha.withMinute(0).withSecond(0).withNano(0);
        if (mantenimientos.tecnicoTieneMantenimientoEnDia(tecnico.getId(), inicioHora, inicioHora.plusHours(1), excluirId))
            throw new ConflictException("El técnico ya tiene un mantenimiento asignado en esa fecha y hora");
    }
    private Mantenimiento buscar(Long id) { return mantenimientos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Mantenimiento " + id + " no encontrado")); }
    private Equipo buscarEquipo(Long id) { return equipos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Equipo " + id + " no encontrado")); }
    private Tecnico buscarTecnico(Long id) { return tecnicos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Técnico " + id + " no encontrado")); }
}
