package ec.edu.espe.mantenimiento;

import ec.edu.espe.mantenimiento.async.NotificacionService;
import ec.edu.espe.mantenimiento.dto.MantenimientoRequest;
import ec.edu.espe.mantenimiento.entity.Equipo;
import ec.edu.espe.mantenimiento.entity.Tecnico;
import ec.edu.espe.mantenimiento.exception.BusinessRuleException;
import ec.edu.espe.mantenimiento.exception.ConflictException;
import ec.edu.espe.mantenimiento.repository.EquipoRepository;
import ec.edu.espe.mantenimiento.repository.MantenimientoRepository;
import ec.edu.espe.mantenimiento.repository.OrdenTrabajoRepository;
import ec.edu.espe.mantenimiento.repository.TecnicoRepository;
import ec.edu.espe.mantenimiento.service.MantenimientoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MantenimientoServiceTest {
    @Mock MantenimientoRepository mantenimientos;
    @Mock EquipoRepository equipos;
    @Mock TecnicoRepository tecnicos;
    @Mock OrdenTrabajoRepository ordenes;
    @Mock NotificacionService notificacionService;
    @InjectMocks MantenimientoService service;
    Equipo equipo;
    Tecnico tecnico;
    MantenimientoRequest request;

    @BeforeEach
    void setUp() {
        equipo = new Equipo("EQ-UNIT", "Bomba", "Taller");
        tecnico = new Tecnico("Ana", "ana@unit.test", "Mecánica");
        request = new MantenimientoRequest(1L, 2L, LocalDateTime.now().plusDays(2), "Prueba unitaria");
        when(equipos.findById(1L)).thenReturn(Optional.of(equipo));
        when(tecnicos.findById(2L)).thenReturn(Optional.of(tecnico));
    }

    @Test
    void rechazaMantenimientoEnFechaPasada() {
        MantenimientoRequest pasado = new MantenimientoRequest(1L, 2L, LocalDateTime.now().minusDays(1), "Pasado");
        assertThrows(BusinessRuleException.class, () -> service.crear(pasado));
        verify(mantenimientos, never()).save(any());
    }

    @Test
    void rechazaEquipoInactivo() {
        equipo.setActivo(false);
        assertThrows(BusinessRuleException.class, () -> service.crear(request));
        verify(mantenimientos, never()).save(any());
    }

    @Test
    void rechazaTecnicoInactivo() {
        tecnico.setActivo(false);
        assertThrows(BusinessRuleException.class, () -> service.crear(request));
        verify(mantenimientos, never()).save(any());
    }

    @Test
    void rechazaTecnicoConAsignacionEnLaMismaHora() {
        when(mantenimientos.tecnicoTieneMantenimientoEnDia(isNull(), any(), any(), isNull())).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.crear(request));
        verify(mantenimientos, never()).save(any());
    }
}
