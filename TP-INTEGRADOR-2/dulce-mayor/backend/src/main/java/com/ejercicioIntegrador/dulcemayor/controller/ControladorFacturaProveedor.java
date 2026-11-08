package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.BaseController;
import com.ejercicioIntegrador.dulcemayor.controller.base.BaseUseCaseController;
import com.ejercicioIntegrador.dulcemayor.controller.base.RespuestaPdf;
import com.ejercicioIntegrador.dulcemayor.dto.FacturaProveedorRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.FacturaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioFacturaProveedor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Facturas de proveedores (ADMINISTRATIVO y JEFE): alta, modificación, consultas y PDF. Sin baja. */
@RestController
@RequestMapping("/api/facturas/proveedores")
public class ControladorFacturaProveedor extends BaseController<FacturaProveedorRequestDTO, FacturaResponseDTO> {

    private final ServicioFacturaProveedor servicioFacturaProveedor;

    public ControladorFacturaProveedor(ServicioFacturaProveedor servicioFacturaProveedor) {
        super(servicioFacturaProveedor);
        this.servicioFacturaProveedor = servicioFacturaProveedor;
    }

    @Override
    protected boolean operacionHabilitada(BaseUseCaseController casoDeUso) {
        return casoDeUso != BaseUseCaseController.BAJA;
    }

    @GetMapping("/proveedor/{idProveedor}")
    public ResponseEntity<List<FacturaResponseDTO>> porProveedor(@PathVariable String idProveedor) throws MiException {
        return ResponseEntity.ok(servicioFacturaProveedor.listarFacturaPorProveedor(idProveedor));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable String id) throws MiException {
        FacturaResponseDTO factura = servicioFacturaProveedor.consultar(id);
        return RespuestaPdf.de(servicioFacturaProveedor.generarPdf(id),
                "FacturaProveedor_" + factura.cuitProveedor() + "_" + factura.numeroFactura() + ".pdf");
    }
}
