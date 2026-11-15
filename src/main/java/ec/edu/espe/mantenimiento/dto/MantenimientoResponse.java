package ec.edu.espe.mantenimiento.dto;

import ec.edu.espe.mantenimiento.entity.Mantenimiento;
import ec.edu.espe.mantenimiento.entity.EstadoMantenimiento;
import java.time.LocalDateTime;

public record MantenimientoResponse(Long id, Long equipoId, String equipoSerial, Long tecnicoId, String tecnicoNombre,
                                    LocalDateTime fechaProgramada, String descripcion, EstadoMantenimiento estado) {
    public static MantenimientoResponse from(Mantenimiento m) {
        return new MantenimientoResponse(m.getId(), m.getEquipo().getId(), m.getEquipo().getSerial(),
                m.getTecnico().getId(), m.getTecnico().getNombres(), m.getFechaProgramada(), m.getDescripcion(), m.getEstado());
    }
}
