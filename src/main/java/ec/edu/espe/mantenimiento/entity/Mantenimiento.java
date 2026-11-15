package ec.edu.espe.mantenimiento.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mantenimientos")
public class Mantenimiento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mantenimiento_equipo"))
    private Equipo equipo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tecnico_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mantenimiento_tecnico"))
    private Tecnico tecnico;
    @Column(name = "fecha_programada", nullable = false)
    private LocalDateTime fechaProgramada;
    @Column(nullable = false, length = 500)
    private String descripcion;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private EstadoMantenimiento estado = EstadoMantenimiento.PROGRAMADO;

    protected Mantenimiento() {}
    public Mantenimiento(Equipo equipo, Tecnico tecnico, LocalDateTime fechaProgramada, String descripcion) {
        this.equipo = equipo; this.tecnico = tecnico; this.fechaProgramada = fechaProgramada; this.descripcion = descripcion;
    }
    public Long getId() { return id; }
    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo equipo) { this.equipo = equipo; }
    public Tecnico getTecnico() { return tecnico; }
    public void setTecnico(Tecnico tecnico) { this.tecnico = tecnico; }
    public LocalDateTime getFechaProgramada() { return fechaProgramada; }
    public void setFechaProgramada(LocalDateTime fechaProgramada) { this.fechaProgramada = fechaProgramada; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public EstadoMantenimiento getEstado() { return estado; }
    public void setEstado(EstadoMantenimiento estado) { this.estado = estado; }
}
