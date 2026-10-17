package com.incisog.Clinica.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
public class FotoPaciente {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String nombre;   // nombre original del archivo, ej: "juan.jpg"
    private String mime;     // tipo de contenido, ej: "image/jpeg"

    // @Lob = "objeto grande". LONGBLOB en MySQL admite archivos de hasta 4 GB;
    // sin esto la columna podría quedar chica para una imagen.
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] contenido;

    private boolean eliminado = false;

    public FotoPaciente() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getMime() { return mime; }
    public void setMime(String mime) { this.mime = mime; }

    public byte[] getContenido() { return contenido; }
    public void setContenido(byte[] contenido) { this.contenido = contenido; }

    public boolean isEliminado() { return eliminado; }
    public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }

}
