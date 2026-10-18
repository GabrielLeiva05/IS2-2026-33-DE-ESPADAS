package com.tienda.carrito.controller.view;

import com.tienda.carrito.business.domain.entity.Proveedor;
import com.tienda.carrito.business.logic.service.ProveedorService;
import com.tienda.carrito.dto.ProveedorUbicacionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * ABM de Proveedores (solo ADMIN). Hereda todo el flujo de BaseController.
 * Agrega el mapa de OpenStreetMap (Leaflet):
 *   GET /admin/proveedor/mapa         -> pantalla con el mapa
 *   GET /admin/proveedor/ubicaciones  -> JSON con los proveedores que tienen latitud y longitud
 */
@Controller
@RequestMapping("/admin/proveedor")
public class ProveedorController extends BaseController<Proveedor, String> {

    private static final Logger log = LoggerFactory.getLogger(ProveedorController.class);

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService service) {
        super(service);
        this.proveedorService = service;
        initController(new Proveedor(), "Listado de proveedores", "Proveedor");
    }

    @GetMapping("/mapa")
    public String mapa(Model model) {
        model.addAttribute("titleMapa", "Mapa de proveedores");
        model.addAttribute("basePath", "/admin/proveedor");
        return "view/mProveedor";
    }

    @GetMapping("/ubicaciones")
    @ResponseBody
    public ResponseEntity<List<ProveedorUbicacionDTO>> ubicaciones() {
        try {
            List<ProveedorUbicacionDTO> ubicaciones = proveedorService.listarConUbicacion().stream()
                    .map(p -> new ProveedorUbicacionDTO(p.getId(), p.getNombre(), p.getDireccion(),
                            p.getLatitud(), p.getLongitud()))
                    .toList();
            return ResponseEntity.ok(ubicaciones);
        } catch (Exception e) {
            log.error("Error al obtener las ubicaciones de proveedores", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
