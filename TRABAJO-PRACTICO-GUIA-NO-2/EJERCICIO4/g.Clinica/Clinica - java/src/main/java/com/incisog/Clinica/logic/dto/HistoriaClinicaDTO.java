package com.incisog.Clinica.logic.dto;

/**
 * Forma de la historia PARA LA API.
 * "Aplana" las relaciones: en vez de objetos anidados ({"paciente": {"id": ...}})
 * viajan solo los ids, que es lo que el frontend realmente necesita.
 * Los detalles no van acá: se piden por /api/historias/{id}/detalles.
 */
public class HistoriaClinicaDTO {

    private String id;
    private String pacienteId;
    private String usuarioId;

    public HistoriaClinicaDTO() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
}
