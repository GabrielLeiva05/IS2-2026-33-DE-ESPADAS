package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.ResultadoVisita;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Parada de una ruta («embeddable» del diagrama). Ruta 1 ◆── 1..* PuntoRuta * ──> 1 PuntoVenta.
 * Cambio al diagrama: "visitado: boolean" se reemplaza por "resultado" (con pedido, sin pedido, entregado,
 * no entregado) y se agrega "motivo". "Visitado" se deduce: resultado distinto de PENDIENTE.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class PuntoRuta {

    @Column(nullable = false)
    private int orden;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResultadoVisita resultado = ResultadoVisita.PENDIENTE;

    @Column(length = 255)
    private String motivo;

    private LocalDateTime fechaHoraVisita;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "punto_venta_id", nullable = false)
    private PuntoVenta puntoVenta;

    public PuntoRuta(PuntoVenta puntoVenta, int orden) {
        this.puntoVenta = puntoVenta;
        this.orden = orden;
    }

    public boolean isVisitado() {
        return resultado != null && !resultado.esPendiente();
    }

    /** marcarVisitado() del diagrama, con el resultado de la visita. */
    public void marcarVisitado(ResultadoVisita nuevoResultado, String nuevoMotivo, LocalDateTime cuando) {
        this.resultado = nuevoResultado;
        this.motivo = nuevoMotivo;
        this.fechaHoraVisita = cuando;
    }
}
