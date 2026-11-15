package ec.edu.espe.mantenimiento.repository;

import ec.edu.espe.mantenimiento.entity.EstadoMantenimiento;
import ec.edu.espe.mantenimiento.entity.Mantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Long> {
    List<Mantenimiento> findByEstadoAndFechaProgramadaGreaterThanEqualOrderByFechaProgramadaAsc(
            EstadoMantenimiento estado, LocalDateTime desde);

    @Query("select m from Mantenimiento m where m.tecnico.id = :tecnicoId order by m.fechaProgramada asc")
    List<Mantenimiento> buscarPorTecnico(@Param("tecnicoId") Long tecnicoId);

    boolean existsByEquipoIdAndEstado(Long equipoId, EstadoMantenimiento estado);
    boolean existsByTecnicoIdAndEstado(Long tecnicoId, EstadoMantenimiento estado);

    @Query("select count(m) > 0 from Mantenimiento m where m.tecnico.id = :tecnicoId " +
           "and m.estado = ec.edu.espe.mantenimiento.entity.EstadoMantenimiento.PROGRAMADO " +
           "and m.fechaProgramada >= :inicio and m.fechaProgramada < :fin and (:excluirId is null or m.id <> :excluirId)")
    boolean tecnicoTieneMantenimientoEnDia(@Param("tecnicoId") Long tecnicoId,
            @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("excluirId") Long excluirId);
}
