package ec.edu.espe.mantenimiento.dto;

import ec.edu.espe.mantenimiento.entity.EstadoOrden;
import jakarta.validation.constraints.NotNull;

public record EstadoOrdenRequest(@NotNull EstadoOrden estado) {}
