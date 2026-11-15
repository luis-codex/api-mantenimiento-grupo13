package ec.edu.espe.mantenimiento.dto;

import ec.edu.espe.mantenimiento.entity.EstadoOrden;
import ec.edu.espe.mantenimiento.entity.OrdenTrabajo;
import java.time.LocalDateTime;

public record OrdenTrabajoResponse(Long id, Long mantenimientoId, String equipoSerial, String descripcion,
                                   EstadoOrden estado, LocalDateTime creadaEn, LocalDateTime actualizadaEn) {
    public static OrdenTrabajoResponse from(OrdenTrabajo o) {
        return new OrdenTrabajoResponse(o.getId(), o.getMantenimiento().getId(),
                o.getMantenimiento().getEquipo().getSerial(), o.getDescripcion(), o.getEstado(), o.getCreadaEn(), o.getActualizadaEn());
    }
}
