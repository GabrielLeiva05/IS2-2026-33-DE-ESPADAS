package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.service.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMINISTRATIVO')")
public class AdminControlador {

    private final PaisServicio paisServicio;
    private final ProvinciaServicio provinciaServicio;
    private final DepartamentoServicio departamentoServicio;
    private final LocalidadServicio localidadServicio;
    private final DireccionServicio direccionServicio;
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
    private final ServicioFacturaProveedor servicioFacturaProveedor;

    public AdminControlador(PaisServicio paisServicio,
                            ProvinciaServicio provinciaServicio,
                            DepartamentoServicio departamentoServicio,
                            LocalidadServicio localidadServicio,
                            DireccionServicio direccionServicio,
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
                            ServicioFacturaProveedor servicioFacturaProveedor) {
        this.paisServicio = paisServicio;
        this.provinciaServicio = provinciaServicio;
        this.departamentoServicio = departamentoServicio;
        this.localidadServicio = localidadServicio;
        this.direccionServicio = direccionServicio;
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
        this.servicioFacturaProveedor = servicioFacturaProveedor;
    }

    @GetMapping("/dashboard")
    public String dashboard(ModelMap modelo) {
        // Ubicación
        modelo.addAttribute("paises", paisServicio.listarTodos());
        modelo.addAttribute("pais", null);
        modelo.addAttribute("provincias", provinciaServicio.listarTodas());
        modelo.addAttribute("provincia", null);
        modelo.addAttribute("departamentos", departamentoServicio.listarTodos());
        modelo.addAttribute("departamento", null);
        modelo.addAttribute("localidades", localidadServicio.listarTodas());
        modelo.addAttribute("localidad", null);
        modelo.addAttribute("direcciones", direccionServicio.listarTodas());
        modelo.addAttribute("direccion", null);

        // Catálogo
        modelo.addAttribute("categorias", servicioCategoria.listarTodas());
        modelo.addAttribute("categoria", null);
        modelo.addAttribute("subcategorias", servicioSubCategoria.listarTodas());
        modelo.addAttribute("subcategoria", null);
        modelo.addAttribute("productos", servicioProducto.listarProducto());
        modelo.addAttribute("producto", null);
        modelo.addAttribute("vigencias", servicioVigenciaPrecio.listarVigenciaPrecio());
        modelo.addAttribute("vigencia", null);

        // Personas
        modelo.addAttribute("personas", personaServicio.listarTodas());
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
        modelo.addAttribute("proveedores", servicioProveedor.listarTodos());
        modelo.addAttribute("proveedor", null);
        modelo.addAttribute("facturasCliente", servicioFacturaCliente.listarTodas());
        modelo.addAttribute("facturaCliente", null);
        modelo.addAttribute("facturasProveedor", servicioFacturaProveedor.listarTodas());
        modelo.addAttribute("facturaProveedor", null);
        return "panel.html";
    }
}
