package com.tienda.carrito.controller.view;

import com.tienda.carrito.business.domain.entity.Articulo;
import com.tienda.carrito.business.domain.entity.Proveedor;
import com.tienda.carrito.business.logic.error.ErrorServiceException;
import com.tienda.carrito.business.logic.service.ArticuloService;
import com.tienda.carrito.business.logic.service.ProveedorService;
import com.tienda.carrito.dto.ArchivoImagen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ABM de Articulos (solo ADMIN). Hereda el flujo de BaseController y completa:
 *  - preUseCase(): carga los proveedores para el combo (via ProveedorService).
 *  - ejecutarActualizacion(): ademas de guardar el articulo, procesa las imagenes subidas.
 */
@Controller
@RequestMapping("/admin/articulo")
public class ArticuloController extends BaseController<Articulo, String> {

    private static final Logger log = LoggerFactory.getLogger(ArticuloController.class);

    private final ArticuloService articuloService;
    private final ProveedorService proveedorService;

    public ArticuloController(ArticuloService service, ProveedorService proveedorService) {
        super(service);
        this.articuloService = service;
        this.proveedorService = proveedorService;
        initController(new Articulo(), "Listado de artículos", "Artículo");
    }

    @Override
    protected void preUseCase(BaseUseCaseController useCase, Model model, Articulo entity) throws ErrorServiceException {
        if (useCase != BaseUseCaseController.LISTAR && useCase != BaseUseCaseController.BAJA) {
            model.addAttribute("proveedores", proveedorService.listarActivos());
            if (entity != null && entity.getProveedor() == null) {
                entity.setProveedor(new Proveedor()); // evita proveedor nulo al dibujar el combo
            }
        }
    }

    @Override
    protected void ejecutarActualizacion(Articulo entity, List<MultipartFile> archivos) throws ErrorServiceException {
        List<ArchivoImagen> imagenes = leerArchivos(archivos);
        if (esNuevo(entity)) {
            articuloService.altaConImagenes(entity, imagenes);
        } else {
            articuloService.modificarConImagenes(entity.getId(), entity, imagenes);
        }
    }

    @PostMapping("/{id}/imagen/{imagenId}/baja")
    public String quitarImagen(@PathVariable String id, @PathVariable String imagenId, RedirectAttributes attributes) {
        try {
            articuloService.quitarImagen(id, imagenId);
            attributes.addFlashAttribute("msgExito", "Imagen eliminada.");
        } catch (ErrorServiceException e) {
            attributes.addFlashAttribute("msgError", e.getMessage());
        } catch (Exception e) {
            log.error("Error al quitar imagen", e);
            attributes.addFlashAttribute("msgError", "Error de Sistemas");
        }
        return "redirect:/admin/articulo/modificar/" + id;
    }

    /** Convierte los MultipartFile (clase web) en ArchivoImagen para que el servicio no dependa de la capa web. */
    private List<ArchivoImagen> leerArchivos(List<MultipartFile> archivos) throws ErrorServiceException {
        List<ArchivoImagen> resultado = new ArrayList<>();
        if (archivos == null) {
            return resultado;
        }
        for (MultipartFile archivo : archivos) {
            if (archivo == null || archivo.isEmpty()) {
                continue; // un input file vacio llega como una parte sin contenido
            }
            try {
                String nombre = archivo.getOriginalFilename() == null ? "imagen" : archivo.getOriginalFilename();
                resultado.add(new ArchivoImagen(nombre, archivo.getContentType(), archivo.getBytes()));
            } catch (IOException e) {
                throw new ErrorServiceException("No se pudo leer el archivo subido.");
            }
        }
        return resultado;
    }
}
