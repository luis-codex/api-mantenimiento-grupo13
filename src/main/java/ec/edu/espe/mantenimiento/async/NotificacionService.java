package ec.edu.espe.mantenimiento.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    @Async("notificacionesExecutor")
    public CompletableFuture<Void> notificarAsignacion(Long tecnicoId, Long mantenimientoId) {
        try {
            log.info(
                    "Enviando notificación de asignación al técnico {} para mantenimiento {}",
                    tecnicoId,
                    mantenimientoId);
            Thread.sleep(2_000);
            log.info(
                    "Notificación enviada al técnico {} para mantenimiento {}",
                    tecnicoId,
                    mantenimientoId);
            return CompletableFuture.completedFuture(null);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(exception);
        }
    }
}
