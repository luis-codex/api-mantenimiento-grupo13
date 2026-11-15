package ec.edu.espe.mantenimiento.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordenes_trabajo")
public class OrdenTrabajo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mantenimiento_id", nullable = false, foreignKey = @ForeignKey(name = "fk_orden_mantenimiento"))
    private Mantenimiento mantenimiento;
    @Column(nullable = false, length = 500)
    private String descripcion;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private EstadoOrden estado = EstadoOrden.PENDIENTE;
    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn = LocalDateTime.now();
    @Column(name = "actualizada_en", nullable = false)
    private LocalDateTime actualizadaEn = LocalDateTime.now();

    protected OrdenTrabajo() {}
    public OrdenTrabajo(Mantenimiento mantenimiento, String descripcion) { this.mantenimiento = mantenimiento; this.descripcion = descripcion; }
    public Long getId() { return id; }
    public Mantenimiento getMantenimiento() { return mantenimiento; }
    public void setMantenimiento(Mantenimiento mantenimiento) { this.mantenimiento = mantenimiento; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public EstadoOrden getEstado() { return estado; }
    public void setEstado(EstadoOrden estado) { this.estado = estado; this.actualizadaEn = LocalDateTime.now(); }
    public LocalDateTime getCreadaEn() { return creadaEn; }
    public LocalDateTime getActualizadaEn() { return actualizadaEn; }
}
