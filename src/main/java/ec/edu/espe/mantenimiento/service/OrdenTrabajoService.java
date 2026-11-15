package ec.edu.espe.mantenimiento.service;

import ec.edu.espe.mantenimiento.dto.*;
import ec.edu.espe.mantenimiento.entity.*;
import ec.edu.espe.mantenimiento.exception.*;
import ec.edu.espe.mantenimiento.repository.MantenimientoRepository;
import ec.edu.espe.mantenimiento.repository.OrdenTrabajoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class OrdenTrabajoService {
    private final OrdenTrabajoRepository ordenes;
    private final MantenimientoRepository mantenimientos;
    public OrdenTrabajoService(OrdenTrabajoRepository ordenes, MantenimientoRepository mantenimientos) { this.ordenes = ordenes; this.mantenimientos = mantenimientos; }
    public OrdenTrabajoResponse crear(OrdenTrabajoRequest request) {
        Mantenimiento m = buscarMantenimiento(request.mantenimientoId());
        if (m.getEstado() == EstadoMantenimiento.CANCELADO) throw new BusinessRuleException("No se puede abrir una orden para un mantenimiento cancelado");
        return OrdenTrabajoResponse.from(ordenes.save(new OrdenTrabajo(m, request.descripcion().trim())));
    }
    @Transactional(readOnly = true)
    public List<OrdenTrabajoResponse> listar(EstadoOrden estado) {
        List<OrdenTrabajo> items = estado == null ? ordenes.findAll() : ordenes.findByEstadoOrderByCreadaEnDesc(estado);
        return items.stream().map(OrdenTrabajoResponse::from).toList();
    }
    @Transactional(readOnly = true)
    public List<OrdenTrabajoResponse> listarPorEquipoYEstado(Long equipoId, EstadoOrden estado) {
        return ordenes.buscarPorEquipoYEstado(equipoId, estado).stream().map(OrdenTrabajoResponse::from).toList();
    }
    @Transactional(readOnly = true)
    public OrdenTrabajoResponse obtener(Long id) { return OrdenTrabajoResponse.from(buscar(id)); }
    public OrdenTrabajoResponse actualizar(Long id, OrdenTrabajoRequest request) {
        OrdenTrabajo o = buscar(id);
        if (o.getEstado() == EstadoOrden.COMPLETADA || o.getEstado() == EstadoOrden.CANCELADA)
            throw new BusinessRuleException("No se puede modificar una orden cerrada");
        Mantenimiento m = buscarMantenimiento(request.mantenimientoId());
        if (m.getEstado() == EstadoMantenimiento.CANCELADO) throw new BusinessRuleException("No se puede asignar una orden a un mantenimiento cancelado");
        o.setMantenimiento(m); o.setDescripcion(request.descripcion().trim());
        return OrdenTrabajoResponse.from(ordenes.save(o));
    }
    public OrdenTrabajoResponse cambiarEstado(Long id, EstadoOrden destino) {
        OrdenTrabajo o = buscar(id);
        EstadoOrden actual = o.getEstado();
        boolean valido = (actual == EstadoOrden.PENDIENTE && (destino == EstadoOrden.EN_PROCESO || destino == EstadoOrden.CANCELADA))
                || (actual == EstadoOrden.EN_PROCESO && (destino == EstadoOrden.COMPLETADA || destino == EstadoOrden.CANCELADA));
        if (!valido) throw new InvalidStateTransitionException("Transición de estado no válida: " + actual + " -> " + destino);
        o.setEstado(destino);
        return OrdenTrabajoResponse.from(ordenes.save(o));
    }
    public void eliminar(Long id) { ordenes.delete(buscar(id)); }
    private OrdenTrabajo buscar(Long id) { return ordenes.findById(id).orElseThrow(() -> new ResourceNotFoundException("Orden de trabajo " + id + " no encontrada")); }
    private Mantenimiento buscarMantenimiento(Long id) { return mantenimientos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Mantenimiento " + id + " no encontrado")); }
}
