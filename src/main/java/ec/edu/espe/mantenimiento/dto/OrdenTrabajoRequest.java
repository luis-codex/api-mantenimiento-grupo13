package ec.edu.espe.mantenimiento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrdenTrabajoRequest(@NotNull Long mantenimientoId, @NotBlank @Size(max = 500) String descripcion) {}
