package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.controller.base.BaseController;
import com.ejercicioIntegrador.dulcemayor.dto.PromocionRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PromocionResponseDTO;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPromocion;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ABM de promociones y códigos de descuento (ADMINISTRATIVO y JEFE). La baja es lógica: los pedidos que ya
 * usaron la promoción conservan su copia del descuento. Para dejar de aplicarla sin eliminarla: activa = false.
 */
@RestController
@RequestMapping("/api/promociones")
public class ControladorPromocion extends BaseController<PromocionRequestDTO, PromocionResponseDTO> {

    public ControladorPromocion(ServicioPromocion servicioPromocion) {
        super(servicioPromocion);
    }
}
