
package com.example.securityDemo.entidades;

import com.example.securityDemo.entidades.Prestamo;
import com.example.securityDemo.enumeraciones.Rol;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.envers.Audited;

@Entity
@Audited
public class Usuario implements Serializable{
    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    private Long id;
    
    @Column(nullable= false, unique= true)
    private Long dni;
    
    
    @Column(nullable= false, length = 100)
    private String nombre;
    
    @Column()
    private String telefono;
    
    @Column()
    private String email;
    
    private String clave;
    
    private boolean alta;
    
    @OneToOne
    @JoinColumn(name = "imagen_id")
    private Imagen imagen;
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prestamo> prestamos = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    private Rol rol;
    
    public Usuario() {
    }

    public Usuario(Long dni, String nombre, String telefono, String email, String clave, Rol rol) {
        this.dni = dni;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.clave = clave;
        this.rol = rol;
    }

    public Imagen getImagen() {
        return imagen;
    }

    public void setImagen(Imagen imagen) {
        this.imagen = imagen;
    }
    
    
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDni() {
        return dni;
    }

    public void setDni(Long dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public Enum getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public boolean isAlta() {
        return alta;
    }

    public void setAlta(boolean alta) {
        this.alta = alta;
    }
    
    
    
    
    
    
    
}
