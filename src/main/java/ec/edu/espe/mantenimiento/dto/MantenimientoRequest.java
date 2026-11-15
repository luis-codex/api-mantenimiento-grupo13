package ec.edu.espe.mantenimiento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record MantenimientoRequest(
        @NotNull Long equipoId,
        @NotNull Long tecnicoId,
        @NotNull LocalDateTime fechaProgramada,
        @NotBlank @Size(max = 500) String descripcion) {}
