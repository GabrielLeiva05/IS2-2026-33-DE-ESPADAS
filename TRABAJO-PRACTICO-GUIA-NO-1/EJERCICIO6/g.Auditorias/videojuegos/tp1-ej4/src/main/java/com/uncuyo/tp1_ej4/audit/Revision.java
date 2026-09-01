package com.uncuyo.tp1_ej4.audit;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

public class Revision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //  numero de revision (equivalente al REV de REVINFO)
    @RevisionNumber
    private int id;

    //fecha/hora (equivalente al REVTSTMP de REVINFO)
    @RevisionTimestamp
    private long fecha;

    // ---- A PARTIR DE ACA, campos custom que Envers NO llena solo ----
    // Si el proyecto tuviera una entidad Usuario (con login/Spring Security),
    // aca iria algo asi:
    //
    // private String usuario;
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
