package com.is2.tinder.audit;

import com.is2.tinder.config.CustomRevisionListener;
import jakarta.persistence.*;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Entity
@Table(name = "revision_info")
@RevisionEntity(CustomRevisionListener.class)
public class Revision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //  numero de revision (equivalente al REV de REVINFO)
    @RevisionNumber
    private int id;

    //fecha/hora (equivalente al REVTSTMP de REVINFO)
    @RevisionTimestamp
    private long fecha;

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    // ---- A PARTIR DE ACA, campos custom que Envers NO llena solo ----
    // Si el proyecto tuviera una entidad Usuario (con login/Spring Security),
    // aca iria algo asi:
    //
    private String usuario;
    //
    // ...y el CustomRevisionListener seria el encargado de completarlo
    // (ver comentario en esa clase). Como este proyecto todavia no tiene
    // usuarios, lo dejamos afuera para no guardar un dato que no existe.

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getFecha() {
        return fecha;
    }

    public void setFecha(long fecha) {
        this.fecha = fecha;
    }
}

