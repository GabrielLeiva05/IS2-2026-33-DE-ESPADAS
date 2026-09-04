package com.example.securityDemo.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.io.Serializable;
import java.util.Date;
import org.hibernate.envers.Audited;

@Entity
@Audited
public class Prestamo implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column
    private Date fechaPrestamo;
    @Column
    private Date fechaDevolucion;
    @Column
    private boolean alta;
    
    @OneToOne
    private Libro libro;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    public Prestamo() {
    }

 
    
    
    
}
