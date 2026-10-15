package com.tienda.carrito.controller.view;

import com.tienda.carrito.business.domain.entity.Proveedor;
import com.tienda.carrito.business.logic.service.ProveedorService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/** ABM de Proveedores (solo ADMIN). Hereda todo el flujo de BaseController. */
@Controller
@RequestMapping("/admin/proveedor")
public class ProveedorController extends BaseController<Proveedor, String> {

    public ProveedorController(ProveedorService service) {
        super(service);
        initController(new Proveedor(), "Listado de proveedores", "Proveedor");
    }
}
