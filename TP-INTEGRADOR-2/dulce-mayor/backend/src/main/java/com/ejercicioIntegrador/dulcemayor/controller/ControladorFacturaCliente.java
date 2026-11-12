package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.BaseController;
import com.ejercicioIntegrador.dulcemayor.controller.base.BaseUseCaseController;
import com.ejercicioIntegrador.dulcemayor.controller.base.RespuestaPdf;
import com.ejercicioIntegrador.dulcemayor.dto.FacturaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SolicitudFacturaClienteDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioFacturaCliente;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Facturas de clientes para administración (ADMINISTRATIVO y JEFE): listado, consulta y PDF.
 * No hay alta por API (la emite la confirmación del pedido) ni modificación ni baja.
 */
@RestController
@RequestMapping("/api/facturas/clientes")
public class ControladorFacturaCliente extends BaseController<SolicitudFacturaClienteDTO, FacturaResponseDTO> {

    private final ServicioFacturaCliente servicioFacturaCliente;

    public ControladorFacturaCliente(ServicioFacturaCliente servicioFacturaCliente) {
        super(servicioFacturaCliente);
        this.servicioFacturaCliente = servicioFacturaCliente;
    }

    @Override
    protected boolean operacionHabilitada(BaseUseCaseController casoDeUso) {
        return casoDeUso == BaseUseCaseController.LISTAR || casoDeUso == BaseUseCaseController.CONSULTAR;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable String id) throws MiException {
        FacturaResponseDTO factura = servicioFacturaCliente.consultar(id);
        return RespuestaPdf.de(servicioFacturaCliente.generarPdf(id), "Factura_DulceMayor_" + factura.numeroFactura() + ".pdf");
    }
}
