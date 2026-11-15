package ec.edu.espe.mantenimiento.repository;

import ec.edu.espe.mantenimiento.entity.EstadoOrden;
import ec.edu.espe.mantenimiento.entity.OrdenTrabajo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, Long> {
    List<OrdenTrabajo> findByEstadoOrderByCreadaEnDesc(EstadoOrden estado);
    boolean existsByMantenimientoEquipoIdAndEstadoIn(Long equipoId, List<EstadoOrden> estados);

    @Query("select o from OrdenTrabajo o join o.mantenimiento m where m.equipo.id = :equipoId and o.estado = :estado order by o.creadaEn desc")
    List<OrdenTrabajo> buscarPorEquipoYEstado(@Param("equipoId") Long equipoId, @Param("estado") EstadoOrden estado);
    boolean existsByMantenimientoIdAndEstadoIn(Long mantenimientoId, List<EstadoOrden> estados);
}
