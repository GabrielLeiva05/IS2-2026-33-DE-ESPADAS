package com.incisog.Clinica.controller.vista;

/**
 * Lo que manda el formulario de "Nueva atención / Modificar atención" de historia.html.
 *
 * Es el equivalente del estado "form" de HistoriaPacientePage.jsx:
 *   { fechaHistoria, medicoId, detalleHistoria } (+ el id cuando se está editando).
 *
 * Todo String porque así llega del formulario HTML; la conversión a LocalDate y a Medico
 * la hace HistoriaVistaController.armarDetalle(). No es una entidad ni va a la base.
 */
public class DetalleFormulario {

    private String id;
    private String fechaHistoria;   // "2026-10-17" (así lo manda <input type="date">)
    private String medicoId;
    private String detalleHistoria;

    public DetalleFormulario() {
    }

    public DetalleFormulario(String id, String fechaHistoria, String medicoId, String detalleHistoria) {
        this.id = id;
        this.fechaHistoria = fechaHistoria;
        this.medicoId = medicoId;
        this.detalleHistoria = detalleHistoria;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFechaHistoria() { return fechaHistoria; }
    public void setFechaHistoria(String fechaHistoria) { this.fechaHistoria = fechaHistoria; }
    public String getMedicoId() { return medicoId; }
    public void setMedicoId(String medicoId) { this.medicoId = medicoId; }
    public String getDetalleHistoria() { return detalleHistoria; }
    public void setDetalleHistoria(String detalleHistoria) { this.detalleHistoria = detalleHistoria; }

    /** true si el formulario está modificando una atención existente. */
    public boolean isEditando() {
        return id != null && !id.isBlank();
    }
}
