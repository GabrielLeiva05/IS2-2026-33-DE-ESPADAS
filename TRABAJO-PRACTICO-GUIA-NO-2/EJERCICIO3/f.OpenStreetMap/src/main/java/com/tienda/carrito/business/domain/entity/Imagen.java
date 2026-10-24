package com.tienda.carrito.business.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/** Diagrama: Imagen (id, nombre, mime, contenido: byte[], eliminado). El binario se guarda en un LONGBLOB. */
@Entity
@Table(name = "imagenes")
@AllArgsConstructor
@NoArgsConstructor
public class Imagen extends BaseEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "mime", nullable = false, length = 100)
    private String mime;

    @Lob
    @Column(name = "contenido", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] contenido;

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Boolean isEliminado() { return eliminado; }
    public void setEliminado(Boolean eliminado) { this.eliminado = eliminado; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getMime() { return mime; }
    public void setMime(String mime) { this.mime = mime; }
    public byte[] getContenido() { return contenido; }
    public void setContenido(byte[] contenido) { this.contenido = contenido; }
}
