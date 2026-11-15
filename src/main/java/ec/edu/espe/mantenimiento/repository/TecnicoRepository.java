package ec.edu.espe.mantenimiento.repository;

import ec.edu.espe.mantenimiento.entity.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {
    boolean existsByEmailIgnoreCase(String email);
    Optional<Tecnico> findByEmailIgnoreCase(String email);

    @Query("select t from Tecnico t where lower(t.especialidad) = lower(:especialidad) and t.activo = true order by t.nombres")
    List<Tecnico> buscarActivosPorEspecialidad(@Param("especialidad") String especialidad);
}
