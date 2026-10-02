package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.service.PaisServicio;
import com.ejercicioIntegrador.tiendaderopa.service.ServicioNewsletter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ejercicioIntegrador.tiendaderopa.service.ProvinciaServicio;
import com.ejercicioIntegrador.tiendaderopa.service.*;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class AdminControlador {

    private final PaisServicio paisServicio;
    private final ProvinciaServicio provinciaServicio;
    private final ServicioNewsletter servicioNewsletter;
    private final ServicioCategoria servicioCategoria;
    private final ServicioSubCategoria servicioSubCategoria;
    private final ServicioProducto servicioProducto;
    private final ServicioVigenciaPrecio servicioVigenciaPrecio;
    private final ServicioFormaDePago servicioFormaDePago;
    private final ServicioEmpresa servicioEmpresa;
    private final ServicioConfiguracionCorreoEmpresa servicioConfiguracion;
    private final PersonaServicio personaServicio;
    private final UsuarioServicio usuarioServicio;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioContactoTelefonico servicioContactoTelefonico;
    private final ServicioContactoCorreoElectronico servicioContactoCorreo;
    private final ServicioProveedor servicioProveedor;
    private final ServicioFacturaCliente servicioFacturaCliente;
    private final ServicioOrdenCompraProveedor servicioOrdenCompra;
    private final ServicioFacturaProveedor servicioFacturaProveedor;
    public AdminControlador(PaisServicio paisServicio, ProvinciaServicio provinciaServicio, ServicioNewsletter servicioNewsletter,
                            ServicioCategoria servicioCategoria,
                            ServicioSubCategoria servicioSubCategoria,
                            ServicioProducto servicioProducto,
                            ServicioVigenciaPrecio servicioVigenciaPrecio,
                            ServicioFormaDePago servicioFormaDePago,
                            ServicioEmpresa servicioEmpresa,
                            ServicioConfiguracionCorreoEmpresa servicioConfiguracion,
                            PersonaServicio personaServicio,
                            UsuarioServicio usuarioServicio,
                            ServicioEmpleado servicioEmpleado,
                            ServicioContactoTelefonico servicioContactoTelefonico,
                            ServicioContactoCorreoElectronico servicioContactoCorreo,
                            ServicioProveedor servicioProveedor,
                            ServicioFacturaCliente servicioFacturaCliente,
                            ServicioOrdenCompraProveedor servicioOrdenCompra,
                            ServicioFacturaProveedor servicioFacturaProveedor) {
        this.paisServicio = paisServicio;
        this.provinciaServicio = provinciaServicio;
        this.servicioNewsletter = servicioNewsletter;
        this.servicioCategoria = servicioCategoria;
        this.servicioSubCategoria = servicioSubCategoria;
        this.servicioProducto = servicioProducto;
        this.servicioVigenciaPrecio = servicioVigenciaPrecio;
        this.servicioFormaDePago = servicioFormaDePago;
        this.servicioEmpresa = servicioEmpresa;
        this.servicioConfiguracion = servicioConfiguracion;
        this.personaServicio = personaServicio;
        this.usuarioServicio = usuarioServicio;
        this.servicioEmpleado = servicioEmpleado;
        this.servicioContactoTelefonico = servicioContactoTelefonico;
        this.servicioContactoCorreo = servicioContactoCorreo;
        this.servicioProveedor = servicioProveedor;
        this.servicioFacturaCliente = servicioFacturaCliente;
        this.servicioOrdenCompra = servicioOrdenCompra;
        this.servicioFacturaProveedor = servicioFacturaProveedor;
    }
    @GetMapping("/dashboard")
    public String dashboard(ModelMap modelo) throws Exception {
        modelo.addAttribute("paises", paisServicio.listarTodos());
        modelo.addAttribute("pais", null);
        modelo.addAttribute("provincias", provinciaServicio.listarTodas());
        modelo.addAttribute("provincia", null);
        modelo.addAttribute("envios", servicioNewsletter.listarEnvios());

        // Catálogo
        modelo.addAttribute("categorias", servicioCategoria.findAll());
        modelo.addAttribute("categoria", null);
        modelo.addAttribute("subcategorias", servicioSubCategoria.findAll());
        modelo.addAttribute("subcategoria", null);
        modelo.addAttribute("productos", servicioProducto.listarProducto());
        modelo.addAttribute("producto", null);
        modelo.addAttribute("vigencias", servicioVigenciaPrecio.listarVigenciaPrecio());
        modelo.addAttribute("vigencia", null);

        // Personas
        modelo.addAttribute("personas", personaServicio.listarPersona());
        modelo.addAttribute("persona", null);
        modelo.addAttribute("usuarios", usuarioServicio.listarTodos());
        modelo.addAttribute("usuario", null);
        modelo.addAttribute("empleados", servicioEmpleado.listarEmpleado());
        modelo.addAttribute("empleado", null);
        modelo.addAttribute("contactosTelefonicos", servicioContactoTelefonico.listarContactoTelefonico());
        modelo.addAttribute("contactoTelefonico", null);
        modelo.addAttribute("contactosCorreo", servicioContactoCorreo.listarContactoCorreoElectronico());
        modelo.addAttribute("contactoCorreo", null);

        // Empresa
        modelo.addAttribute("empresas", servicioEmpresa.listarEmpresa());
        modelo.addAttribute("empresa", null);
        modelo.addAttribute("configuracionesCorreo", servicioConfiguracion.listarConfiguracionCorreoAutomatico());
        modelo.addAttribute("configuracionCorreo", null);

        // Facturación
        modelo.addAttribute("formasDePago", servicioFormaDePago.listarFormaDePago());
        modelo.addAttribute("formaDePago", null);
        modelo.addAttribute("proveedores", servicioProveedor.listarProveedor());
        modelo.addAttribute("proveedor", null);
        modelo.addAttribute("ordenesCompra", servicioOrdenCompra.listarOrdenCompraProveedor());
        modelo.addAttribute("facturasCliente", servicioFacturaCliente.listarTodas());
        modelo.addAttribute("facturaCliente", null);
        modelo.addAttribute("facturasProveedor", servicioFacturaProveedor.listarTodas());
        modelo.addAttribute("facturaProveedor", null);
        return "panel.html";
    }
}