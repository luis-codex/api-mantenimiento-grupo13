package ec.edu.espe.mantenimiento.repository;

import ec.edu.espe.mantenimiento.entity.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    boolean existsBySerialIgnoreCase(String serial);
    Optional<Equipo> findBySerialIgnoreCase(String serial);
}
