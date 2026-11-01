package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.BaseController;
import com.ejercicioIntegrador.dulcemayor.controller.base.BaseUseCaseController;
import com.ejercicioIntegrador.dulcemayor.dto.DisponibilidadStockDTO;
import com.ejercicioIntegrador.dulcemayor.dto.MovimientoStockDTO;
import com.ejercicioIntegrador.dulcemayor.dto.StockRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.StockResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioStock;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class ControladorStock extends BaseController<StockRequestDTO, StockResponseDTO> {

    private final ServicioStock servicioStock;

    public ControladorStock(ServicioStock servicioStock) {
        super(servicioStock);
        this.servicioStock = servicioStock;
    }

    /**
     * Paso redefinido del Template Method: el stock no se modifica por PUT ni se da de baja.
     * La cantidad cambia únicamente con /reponer y /descontar.
     */
    @Override
    protected boolean operacionHabilitada(BaseUseCaseController casoDeUso) {
        return casoDeUso != BaseUseCaseController.MODIFICACION && casoDeUso != BaseUseCaseController.BAJA;
    }

    @GetMapping("/producto/{idProducto}")
    public ResponseEntity<StockResponseDTO> buscarPorProducto(@PathVariable String idProducto) throws MiException {
        return ResponseEntity.ok(servicioStock.buscarStockPorProducto(idProducto));
    }

    @GetMapping("/producto/{idProducto}/disponibilidad")
    public ResponseEntity<DisponibilidadStockDTO> disponibilidad(@PathVariable String idProducto,
                                                                 @RequestParam Integer cantidad) throws MiException {
        return ResponseEntity.ok(servicioStock.consultarDisponibilidad(idProducto, cantidad));
    }

    @PatchMapping("/producto/{idProducto}/reponer")
    public ResponseEntity<StockResponseDTO> reponer(@PathVariable String idProducto,
                                                    @RequestBody MovimientoStockDTO movimiento) throws MiException {
        return ResponseEntity.ok(servicioStock.reponerStock(idProducto, movimiento.cantidad()));
    }

    @PatchMapping("/producto/{idProducto}/descontar")
    public ResponseEntity<StockResponseDTO> descontar(@PathVariable String idProducto,
                                                      @RequestBody MovimientoStockDTO movimiento) throws MiException {
        return ResponseEntity.ok(servicioStock.descontarStock(idProducto, movimiento.cantidad()));
    }

    @GetMapping("/sin-stock")
    public ResponseEntity<List<StockResponseDTO>> sinStock() {
        return ResponseEntity.ok(servicioStock.listarProductoSinStock());
    }
}
