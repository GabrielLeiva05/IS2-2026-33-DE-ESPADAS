package com.incisog.Clinica.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String nombre;
    private String apellido;
    private String documento;
    private boolean eliminado = false;

    // Diagrama: Paciente *..1 FotoPaciente.
    // @JsonIgnore: la foto (byte[]) NO viaja en el JSON del paciente; se pide aparte
    // con GET /api/pacientes/{id}/foto. Así un listado de pacientes no arrastra imágenes.
    @ManyToOne
    @JsonIgnore
    private FotoPaciente foto;

    public Paciente() {
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public boolean isEliminado() { return eliminado; }
    public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }

    public FotoPaciente getFoto() { return foto; }
    public void setFoto(FotoPaciente foto) { this.foto = foto; }

}
