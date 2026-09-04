package com.example.securityDemo.controladores;

import com.example.securityDemo.entidades.Usuario;
import com.example.securityDemo.exceptions.MiException;
import com.example.securityDemo.servicio.UsuarioServicio;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/")
public class PortalControlador {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/")
    public String index() {
        return "index.html";
    }

    @GetMapping("/registrar")
    public String registrar() {
        return "registro.html";
    }

    @PostMapping("/registro")
    public String registro(
            @RequestParam(required = false) MultipartFile archivo,
            @RequestParam(required = false) Long dni,
            @RequestParam String nombre,
            @RequestParam String telefono,
            @RequestParam String email,
            @RequestParam String clave,
            @RequestParam String clave2,
            ModelMap modelo) {
        try {
            usuarioServicio.registrar(archivo, dni, nombre, telefono,
                    email, clave, clave2);
            modelo.put("exito", "Usuario registrado correctamente");
            return "index.html";
        } catch (MiException ex) {
            modelo.put("error", ex.getMessage());
            modelo.put("dni", dni);
            modelo.put("nombre", nombre);
            modelo.put("telefono", telefono);
            modelo.put("email", email);
            return "registro.html";
        } catch (Exception ex) {
            modelo.put("error", "Error inesperado del sistema: " + ex.getMessage());
            return "registro.html";
        }
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, ModelMap modelo) {
        if (error != null) {
            modelo.put("error", "Usuario o contraseña inválidos");
        }

        return "login.html";
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/inicio")
    public String inicio() {
        return "index.html";
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/perfil")
    public String perfil(ModelMap modelo, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");
        modelo.put("usuario", usuario);
        return "usuario_modificar.html";
    }
    
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
@PostMapping("/perfil/{id}")
public String actualizar(MultipartFile archivo, 
        @PathVariable Long id, 
                         @RequestParam String nombre, 
                         @RequestParam String email,
                         @RequestParam String clave, 
                         @RequestParam String clave2, 
                         ModelMap modelo) throws MiException {

    usuarioServicio.actualizar(archivo, id, nombre, email, clave, clave2);
    modelo.put("exito", "Usuario actualizado correctamente!");
    return "index.html";
}
}
