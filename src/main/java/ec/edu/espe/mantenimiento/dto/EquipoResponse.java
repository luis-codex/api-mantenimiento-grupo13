package ec.edu.espe.mantenimiento.dto;

import ec.edu.espe.mantenimiento.entity.Equipo;

public record EquipoResponse(Long id, String serial, String nombre, String ubicacion, boolean activo) {
    public static EquipoResponse from(Equipo e) { return new EquipoResponse(e.getId(), e.getSerial(), e.getNombre(), e.getUbicacion(), e.isActivo()); }
}
