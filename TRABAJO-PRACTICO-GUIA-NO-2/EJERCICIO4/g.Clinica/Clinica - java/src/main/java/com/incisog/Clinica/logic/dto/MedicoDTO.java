package com.incisog.Clinica.logic.dto;

/** Forma del médico PARA LA API (sin "eliminado"). Mismos nombres de campos que antes: React no cambia. */
public class MedicoDTO {

    private String id;
    private String nombre;
    private String apellido;
    private String documento;

    public MedicoDTO() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
}
