package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.BaseController;
import com.ejercicioIntegrador.dulcemayor.controller.base.BaseUseCaseController;
import com.ejercicioIntegrador.dulcemayor.dto.AsignacionPedidosDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ConfirmarPedidoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoAltaDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SimulacionPedidoDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ResultadoVisitaDTO;
import com.ejercicioIntegrador.dulcemayor.dto.RutaDelDiaDTO;
import com.ejercicioIntegrador.dulcemayor.dto.UbicacionDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPedido;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Pedidos.
 *  - Heredados (administración): listar y consultar. Alta, modificación y baja genéricas deshabilitadas:
 *    la carga la hace el preventista (/preventista) y la anulación el cliente.
 *  - /preventista: carga (HU-21) y "Mis pedidos" del preventista autenticado.
 *  - /mis-pedidos: el cliente autenticado consulta, confirma o anula SOLO sus pedidos (siempre por POST).
 *  - /entregas: el distribuidor autenticado genera su ruta, entrega o registra una entrega no concretada.
 *  - /asignacion y /estado: administración.
 */
@RestController
@RequestMapping("/api/pedidos")
public class ControladorPedido extends BaseController<PedidoAltaDTO, PedidoResponseDTO> {

    private final ServicioPedido servicioPedido;

    public ControladorPedido(ServicioPedido servicioPedido) {
        super(servicioPedido);
        this.servicioPedido = servicioPedido;
    }

    @Override
    protected boolean operacionHabilitada(BaseUseCaseController casoDeUso) {
        return casoDeUso == BaseUseCaseController.LISTAR || casoDeUso == BaseUseCaseController.CONSULTAR;
    }

    // ---------------- Administración ----------------

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PedidoResponseDTO>> listarPorEstado(@PathVariable EstadoPedido estado) throws MiException {
        return ResponseEntity.ok(servicioPedido.listarPedidoPorEstado(estado));
    }

    @PostMapping("/asignacion")
    public ResponseEntity<List<PedidoResponseDTO>> asignar(@RequestBody AsignacionPedidosDTO asignacion)
            throws MiException {
        return ResponseEntity.ok(servicioPedido.asignarDistribuidor(asignacion));
    }

    // ---------------- Preventista ----------------

    @PostMapping("/preventista")
    public ResponseEntity<PedidoResponseDTO> cargar(Authentication autenticacion, @RequestBody PedidoRequestDTO pedido)
            throws MiException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicioPedido.cargarPedido(autenticacion.getName(), pedido));
    }

    /** Vista previa con promociones y código de descuento, calculada en el backend (no guarda nada). */
    @PostMapping("/preventista/simular")
    public ResponseEntity<SimulacionPedidoDTO> simular(@RequestBody PedidoRequestDTO pedido) throws MiException {
        return ResponseEntity.ok(servicioPedido.simularPedido(pedido));
    }

    @GetMapping("/preventista")
    public ResponseEntity<List<PedidoResponseDTO>> pedidosDelPreventista(Authentication autenticacion)
            throws MiException {
        return ResponseEntity.ok(servicioPedido.listarPedidosDelPreventista(autenticacion.getName()));
    }

    @GetMapping("/preventista/{id}")
    public ResponseEntity<PedidoResponseDTO> pedidoDelPreventista(Authentication autenticacion, @PathVariable String id)
            throws MiException {
        return ResponseEntity.ok(servicioPedido.consultarPedidoDelPreventista(autenticacion.getName(), id));
    }

    /** Confirmación presencial: el preventista elige el medio de pago preferido por el cliente. */
    @PostMapping("/preventista/{id}/confirmar")
    public ResponseEntity<PedidoResponseDTO> confirmarPresencial(Authentication autenticacion, @PathVariable String id,
                                                                 @RequestBody ConfirmarPedidoRequestDTO confirmacion)
            throws MiException {
        return ResponseEntity.ok(servicioPedido.confirmarPedidoPresencial(autenticacion.getName(), id,
                confirmacion == null ? null : confirmacion.formaDePago()));
    }

    // ---------------- Cliente ----------------

    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<PedidoResponseDTO>> misPedidos(Authentication autenticacion) throws MiException {
        return ResponseEntity.ok(servicioPedido.listarPedidosDelCliente(autenticacion.getName()));
    }

    @GetMapping("/mis-pedidos/{id}")
    public ResponseEntity<PedidoResponseDTO> miPedido(Authentication autenticacion, @PathVariable String id)
            throws MiException {
        return ResponseEntity.ok(servicioPedido.consultarPedidoDelCliente(autenticacion.getName(), id));
    }

    @PostMapping("/mis-pedidos/{id}/confirmar")
    public ResponseEntity<PedidoResponseDTO> confirmar(Authentication autenticacion, @PathVariable String id)
            throws MiException {
        return ResponseEntity.ok(servicioPedido.confirmarPedido(autenticacion.getName(), id));
    }

    @PostMapping("/mis-pedidos/{id}/anular")
    public ResponseEntity<PedidoResponseDTO> anular(Authentication autenticacion, @PathVariable String id)
            throws MiException {
        return ResponseEntity.ok(servicioPedido.anularPedido(autenticacion.getName(), id));
    }

    // ---------------- Distribuidor ----------------

    @GetMapping("/entregas/pendientes")
    public ResponseEntity<List<PedidoResponseDTO>> pendientes(Authentication autenticacion) throws MiException {
        return ResponseEntity.ok(servicioPedido.listarPendientesDelDistribuidor(autenticacion.getName()));
    }

    @PostMapping("/entregas/ruta")
    public ResponseEntity<RutaDelDiaDTO> generarRuta(Authentication autenticacion,
                                                     @RequestBody(required = false) UbicacionDTO inicio)
            throws MiException {
        return ResponseEntity.ok(servicioPedido.generarRutaDeEntregas(autenticacion.getName(), inicio));
    }

    @GetMapping("/entregas/ruta")
    public ResponseEntity<RutaDelDiaDTO> consultarRuta(Authentication autenticacion,
                                                       @RequestParam(required = false) Double lat,
                                                       @RequestParam(required = false) Double lon) throws MiException {
        return ResponseEntity.ok(servicioPedido.consultarRutaDeEntregas(autenticacion.getName(), new UbicacionDTO(lat, lon)));
    }

    @PostMapping("/entregas/{idPuntoVenta}/entregado")
    public ResponseEntity<List<PedidoResponseDTO>> entregado(Authentication autenticacion,
                                                             @PathVariable String idPuntoVenta) throws MiException {
        return ResponseEntity.ok(servicioPedido.registrarEntrega(autenticacion.getName(), idPuntoVenta));
    }

    @PostMapping("/entregas/{idPuntoVenta}/no-entregado")
    public ResponseEntity<Void> noEntregado(Authentication autenticacion, @PathVariable String idPuntoVenta,
                                            @RequestBody ResultadoVisitaDTO resultado) throws MiException {
        servicioPedido.registrarEntregaNoConcretada(autenticacion.getName(), idPuntoVenta,
                resultado == null ? null : resultado.motivo());
        return ResponseEntity.noContent().build();
    }
}
