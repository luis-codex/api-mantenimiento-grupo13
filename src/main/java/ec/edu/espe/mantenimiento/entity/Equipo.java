package ec.edu.espe.mantenimiento.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipos", uniqueConstraints = @UniqueConstraint(name = "uk_equipo_serial", columnNames = "serial"))
public class Equipo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 80)
    private String serial;
    @Column(nullable = false, length = 140)
    private String nombre;
    @Column(nullable = false, length = 180)
    private String ubicacion;
    @Column(nullable = false)
    private boolean activo = true;
    @OneToMany(mappedBy = "equipo")
    private List<Mantenimiento> mantenimientos = new ArrayList<>();

    protected Equipo() {}
    public Equipo(String serial, String nombre, String ubicacion) { this.serial = serial; this.nombre = nombre; this.ubicacion = ubicacion; }
    public Long getId() { return id; }
    public String getSerial() { return serial; }
    public void setSerial(String serial) { this.serial = serial; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
