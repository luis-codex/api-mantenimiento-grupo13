package ec.edu.espe.mantenimiento.dto;

import ec.edu.espe.mantenimiento.entity.Tecnico;

public record TecnicoResponse(Long id, String nombres, String email, String especialidad, boolean activo) {
    public static TecnicoResponse from(Tecnico t) { return new TecnicoResponse(t.getId(), t.getNombres(), t.getEmail(), t.getEspecialidad(), t.isActivo()); }
}
