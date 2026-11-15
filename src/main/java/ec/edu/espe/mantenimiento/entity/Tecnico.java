package ec.edu.espe.mantenimiento.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tecnicos", uniqueConstraints = @UniqueConstraint(name = "uk_tecnico_email", columnNames = "email"))
public class Tecnico {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 140)
    private String nombres;
    @Column(nullable = false, unique = true, length = 180)
    private String email;
    @Column(nullable = false, length = 120)
    private String especialidad;
    @Column(nullable = false)
    private boolean activo = true;

    protected Tecnico() {}
    public Tecnico(String nombres, String email, String especialidad) { this.nombres = nombres; this.email = email; this.especialidad = especialidad; }
    public Long getId() { return id; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
