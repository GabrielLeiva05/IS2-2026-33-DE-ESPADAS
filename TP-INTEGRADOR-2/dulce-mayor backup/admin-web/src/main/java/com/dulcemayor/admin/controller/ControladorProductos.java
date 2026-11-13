package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.cliente.ApiException;
import com.dulcemayor.admin.dto.ProductoDTO;
import com.dulcemayor.admin.dto.ProductoFormularioDTO;
import com.dulcemayor.admin.seguridad.GestorSesion;
import com.dulcemayor.admin.service.ServicioProductos;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * ABM de productos (HU-11, corrección Ola 2): alta, modificación, consulta y baja lógica, más precio, stock y fotos.
 * Disponible para ADMINISTRATIVO y JEFE (InterceptorAcceso). La API vuelve a verificar el rol en cada operación.
 */
@Controller
@RequestMapping("/productos")
public class ControladorProductos {

    private final ServicioProductos servicioProductos;
    private final GestorSesion gestorSesion;

    public ControladorProductos(ServicioProductos servicioProductos, GestorSesion gestorSesion) {
        this.servicioProductos = servicioProductos;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String texto, HttpServletRequest request, Model model) {
        model.addAttribute("filas", servicioProductos.listar(token(request), texto));
        model.addAttribute("texto", texto);
        return "productos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(HttpServletRequest request, Model model) {
        return formulario(new ProductoFormularioDTO(), null, request, model);
    }

    @PostMapping
    public String crear(@ModelAttribute("formulario") ProductoFormularioDTO formulario,
                        @RequestParam(required = false) MultipartFile imagen,
                        HttpServletRequest request, Model model, RedirectAttributes redireccion) {
        try {
            agregarImagen(request, formulario, imagen);
            ProductoDTO creado = servicioProductos.crear(token(request), formulario);
            redireccion.addFlashAttribute("exito", "Se dio de alta " + creado.getNombre() + ".");
            return "redirect:/productos/" + creado.getId() + "/editar";
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            return formulario(formulario, e.getMessage(), request, model);
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, HttpServletRequest request, Model model) {
        ProductoDTO producto = servicioProductos.obtener(token(request), id);
        return formulario(ProductoFormularioDTO.desde(producto), null, request, model);
    }

    @PostMapping("/{id}")
    public String modificar(@PathVariable String id, @ModelAttribute("formulario") ProductoFormularioDTO formulario,
                            @RequestParam(required = false) MultipartFile imagen,
                            HttpServletRequest request, Model model, RedirectAttributes redireccion) {
        formulario.setId(id);
        try {
            agregarImagen(request, formulario, imagen);
            ProductoDTO modificado = servicioProductos.modificar(token(request), id, formulario);
            redireccion.addFlashAttribute("exito", "Se guardaron los cambios de " + modificado.getNombre() + ".");
            return "redirect:/productos/" + id + "/editar";
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            return formulario(formulario, e.getMessage(), request, model);
        }
    }

    @PostMapping("/{id}/baja")
    public String darDeBaja(@PathVariable String id, HttpServletRequest request, RedirectAttributes redireccion) {
        try {
            servicioProductos.darDeBaja(token(request), id);
            redireccion.addFlashAttribute("exito", "El producto fue dado de baja. Los pedidos y facturas anteriores lo conservan.");
            return "redirect:/productos";
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            redireccion.addFlashAttribute("error", e.getMessage());
            return "redirect:/productos/" + id + "/editar";
        }
    }

    @PostMapping("/{id}/precio")
    public String nuevoPrecio(@PathVariable String id, @RequestParam Double precio,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                              HttpServletRequest request, RedirectAttributes redireccion) {
        return accion(() -> servicioProductos.nuevoPrecio(token(request), id, precio, desde),
                "Se registró el nuevo precio.", id, redireccion);
    }

    @PostMapping("/{id}/stock")
    public String reponer(@PathVariable String id, @RequestParam Integer cantidad,
                          @RequestParam(required = false) Integer stockMinimo,
                          HttpServletRequest request, RedirectAttributes redireccion) {
        return accion(() -> servicioProductos.reponerStock(token(request), id, cantidad == null ? 0 : cantidad, stockMinimo),
                "Se actualizó el stock.", id, redireccion);
    }

    /** Subcategorías de una categoría (selects encadenados del formulario). */
    @GetMapping("/subcategorias")
    @ResponseBody
    public List<com.dulcemayor.admin.dto.CategoriaDTO> subcategorias(@RequestParam String idCategoria, HttpServletRequest request) {
        return servicioProductos.subcategorias(token(request), idCategoria);
    }

    // ------------------------------------------------------------------

    private String formulario(ProductoFormularioDTO formulario, String error, HttpServletRequest request, Model model) {
        String token = token(request);
        model.addAttribute("formulario", formulario);
        model.addAttribute("categorias", servicioProductos.categorias(token));
        model.addAttribute("subcategorias", servicioProductos.subcategorias(token, formulario.getIdCategoria()));
        model.addAttribute("presentaciones", List.of("UNIDAD", "PACK", "DISPLAY", "CAJA", "BULTO"));
        if (!formulario.isNuevo()) {
            model.addAttribute("producto", servicioProductos.obtener(token, formulario.getId()));
            model.addAttribute("stock", servicioProductos.stock(token, formulario.getId()).orElse(null));
            model.addAttribute("precios", servicioProductos.precios(token, formulario.getId()));
        }
        model.addAttribute("error", error);
        return "productos/formulario";
    }

    private void agregarImagen(HttpServletRequest request, ProductoFormularioDTO formulario, MultipartFile imagen) {
        if (imagen != null && !imagen.isEmpty()) {
            formulario.getIdsImagen().add(servicioProductos.subirImagen(token(request), imagen));
        }
    }

    private String accion(Runnable operacion, String exito, String id, RedirectAttributes redireccion) {
        try {
            operacion.run();
            redireccion.addFlashAttribute("exito", exito);
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos/" + id + "/editar";
    }

    private String token(HttpServletRequest request) {
        return gestorSesion.actual(request).orElseThrow().getToken();
    }
}
