package ec.edu.espe.mantenimiento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EquipoRequest(
        @NotBlank @Size(max = 80) String serial,
        @NotBlank @Size(max = 140) String nombre,
        @NotBlank @Size(max = 180) String ubicacion,
        Boolean activo) {}
