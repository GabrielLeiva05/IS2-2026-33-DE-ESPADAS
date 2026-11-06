package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.BaseController;
import com.ejercicioIntegrador.dulcemayor.controller.base.BaseUseCaseController;
import com.ejercicioIntegrador.dulcemayor.dto.ResultadoVisitaDTO;
import com.ejercicioIntegrador.dulcemayor.dto.RutaDelDiaDTO;
import com.ejercicioIntegrador.dulcemayor.dto.RutaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.RutaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.UbicacionDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioRuta;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Rutas de visita.
 *  - Administración (heredado + propios): armar la ruta (POST), consultar, dar de baja, agregar y quitar puntos.
 *  - App del preventista (/mia): su ruta de hoy, ordenarla desde su ubicación y registrar visitas sin pedido.
 *    El preventista sale del token: no se recibe ningún id de empleado ni de ruta.
 */
@RestController
@RequestMapping("/api/rutas")
public class ControladorRuta extends BaseController<RutaRequestDTO, RutaResponseDTO> {

    private final ServicioRuta servicioRuta;

    public ControladorRuta(ServicioRuta servicioRuta) {
        super(servicioRuta);
        this.servicioRuta = servicioRuta;
    }

    @Override
    protected boolean operacionHabilitada(BaseUseCaseController casoDeUso) {
        return casoDeUso != BaseUseCaseController.MODIFICACION;
    }

    @GetMapping("/fecha")
    public ResponseEntity<List<RutaResponseDTO>> listarPorFecha(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(servicioRuta.listarRutasDeLaFecha(fecha));
    }

    @PostMapping("/{id}/puntos/{idPuntoVenta}")
    public ResponseEntity<RutaResponseDTO> agregarPunto(@PathVariable String id, @PathVariable String idPuntoVenta)
            throws MiException {
        return ResponseEntity.ok(servicioRuta.agregarPunto(id, idPuntoVenta));
    }

    @DeleteMapping("/{id}/puntos/{idPuntoVenta}")
    public ResponseEntity<RutaResponseDTO> quitarPunto(@PathVariable String id, @PathVariable String idPuntoVenta)
            throws MiException {
        return ResponseEntity.ok(servicioRuta.quitarPunto(id, idPuntoVenta));
    }

    // ---------------- App del preventista ----------------

    @GetMapping("/mia")
    public ResponseEntity<RutaDelDiaDTO> miRuta(Authentication autenticacion,
                                                @RequestParam(required = false) Double lat,
                                                @RequestParam(required = false) Double lon) throws MiException {
        return ResponseEntity.ok(servicioRuta.miRutaDeVisitas(autenticacion.getName(), new UbicacionDTO(lat, lon)));
    }

    @PostMapping("/mia/ordenar")
    public ResponseEntity<RutaDelDiaDTO> ordenar(Authentication autenticacion, @RequestBody UbicacionDTO inicio)
            throws MiException {
        return ResponseEntity.ok(servicioRuta.ordenarMiRutaDeVisitas(autenticacion.getName(), inicio));
    }

    @PostMapping("/mia/puntos/{idPuntoVenta}/sin-pedido")
    public ResponseEntity<Void> visitaSinPedido(Authentication autenticacion, @PathVariable String idPuntoVenta,
                                                @RequestBody(required = false) ResultadoVisitaDTO resultado)
            throws MiException {
        servicioRuta.registrarVisitaSinPedido(autenticacion.getName(), idPuntoVenta,
                resultado == null ? null : resultado.motivo());
        return ResponseEntity.noContent().build();
    }
}
