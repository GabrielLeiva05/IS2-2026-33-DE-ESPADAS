package com.ejercicioIntegrador.dulcemayor.service.cotizacion;

import com.ejercicioIntegrador.dulcemayor.dto.CotizacionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
public class ServicioCotizacion {

    private static final Logger LOG = LoggerFactory.getLogger(ServicioCotizacion.class);
    static final Set<String> TIPOS = Set.of("oficial", "blue", "bolsa", "contadoconliqui", "mayorista", "cripto",
            "tarjeta");
    private static final Duration ESPERA_TRAS_FALLA = Duration.ofMinutes(1);

    private ProveedorCotizacion proveedor;
    private String tipo;
    private Duration vigenciaCache;
    private Clock reloj;

    private Cotizacion ultima;
    private LocalDateTime ultimaConsultaExitosa;
    private LocalDateTime ultimoIntento;
    private boolean ultimaFallo;

    // Constructor por defecto requerido por Spring si intenta instanciación genérica
    public ServicioCotizacion() {
    }

    @Autowired
    public ServicioCotizacion(ProveedorCotizacion proveedor,
                              @Value("${app.cotizacion.tipo:oficial}") String tipo,
                              @Value("${app.cotizacion.cache-minutos:10}") long cacheMinutos) {
        this(proveedor, tipo, cacheMinutos, Clock.systemDefaultZone());
    }

    ServicioCotizacion(ProveedorCotizacion proveedor, String tipo, long cacheMinutos, Clock reloj) {
        String normalizado = tipo == null ? "" : tipo.trim().toLowerCase();
        if (!TIPOS.contains(normalizado)) {
            throw new IllegalArgumentException("Tipo de cotización no soportado: " + tipo + " (opciones: " + TIPOS + ")");
        }
        this.proveedor = proveedor;
        this.tipo = normalizado;
        this.vigenciaCache = Duration.ofMinutes(Math.max(1, cacheMinutos));
        this.reloj = reloj;
    }

    /** Cotización vigente (de la caché o recién consultada); vacío si nunca se pudo obtener. */
    public synchronized Optional<CotizacionDTO> actual() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        boolean cacheVigente = ultima != null && !ultimaFallo
                && ultimaConsultaExitosa.plus(vigenciaCache).isAfter(ahora);
        boolean esperandoTrasFalla = ultimaFallo && ultimoIntento.plus(ESPERA_TRAS_FALLA).isAfter(ahora);
        if (!cacheVigente && !esperandoTrasFalla) {
            consultarFuente(ahora);
        }
        if (ultima == null) {
            return Optional.empty();
        }
        return Optional.of(new CotizacionDTO(ultima.tipo(), ultima.nombre(), ultima.compra(), ultima.venta(),
                ultima.fechaActualizacion(), ultimaConsultaExitosa, ultimaFallo, "DolarAPI (dolarapi.com)"));
    }

    /** Venta vigente para convertir; vacío si no hay cotización. */
    public Optional<BigDecimal> ventaVigente() {
        return actual().map(CotizacionDTO::venta);
    }

    private void consultarFuente(LocalDateTime ahora) {
        ultimoIntento = ahora;
        try {
            ultima = proveedor.consultar(tipo);
            ultimaConsultaExitosa = ahora;
            ultimaFallo = false;
        } catch (RuntimeException e) {
            ultimaFallo = true;
            LOG.warn("No se pudo actualizar la cotización del dólar ({}): {}{}", tipo, e.getMessage(),
                    ultima == null ? "" : " — se usa la última obtenida");
        }
    }
}