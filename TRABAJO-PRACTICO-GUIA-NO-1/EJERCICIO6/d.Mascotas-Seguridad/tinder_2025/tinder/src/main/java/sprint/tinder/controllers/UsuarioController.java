package sprint.tinder.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import sprint.tinder.audit.UsuarioActualHolder;
import sprint.tinder.entities.Usuario;
import sprint.tinder.entities.Zona;
import sprint.tinder.errors.ErrorServicio;
import sprint.tinder.services.UsuarioService;
import sprint.tinder.services.ZonaService;

import java.util.List;

@Controller
@RequestMapping("/usuario") //Indica cual es la url que va a ejecutar este controlador
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private ZonaService zonaService;

    // El POST a /usuario/loginUsuario ahora lo intercepta directamente el filtro de
    // Spring Security (ver SecurityConfig -> formLogin.loginProcessingUrl), que valida
    // el usuario contra UsuarioService.loadUserByUsername(). Este controller ya no
    // necesita un método propio para el login.

    @PostMapping("/registrar")
    public String crearUsuario(ModelMap modelo, MultipartFile archivo, @RequestParam String nombre, @RequestParam String apellido, @RequestParam String mail, @RequestParam String clave1, @RequestParam String clave2, @RequestParam String idZona) {

        try {
            usuarioService.registrar(nombre, apellido, mail, clave1, clave2, archivo, idZona);
            modelo.put("titulo", "Bienvenido al Tinder de Mascotas. ");
            modelo.put("descripcion", "Tu usuario fue registrado de manera satisfactoria. ");
            return "exito.html";

        } catch (ErrorServicio ex) {
            try {
                List<Zona> zonas = zonaService.listarZona();
                modelo.put("zonas", zonas);
            } catch (ErrorServicio e) {}
            modelo.put("error", ex.getMessage());
            modelo.put("nombre", nombre);
            modelo.put("apellido", apellido);
            modelo.put("mail", mail);
            modelo.put("clave1", clave1);
            modelo.put("clave2", clave2);
            return "registro.html";
        }catch(Exception e) {
            e.printStackTrace();
            modelo.put("error", e.getMessage());
            return "registro.html";
        }

    }
    @GetMapping("/editar-perfil")
    public String editarPerfil(HttpSession session, ModelMap model) {
        try {
            List<Zona> zonas = zonaService.listarZona();
            model.put("zonas", zonas);
            Usuario usuario = (Usuario) session.getAttribute("usuariosession");
            model.addAttribute("perfil", usuario);
        } catch (ErrorServicio e) {
            model.addAttribute("error", e.getMessage());
        }
        return "perfil.html";
    }

    @PostMapping("/actualizar-perfil")
    public String modificarUsuario(ModelMap modelo, HttpSession session, MultipartFile archivo, @RequestParam String id, @RequestParam String nombre, @RequestParam String apellido, @RequestParam String mail, @RequestParam String clave1, @RequestParam String clave2, @RequestParam String idZona) {
        Usuario usuario = null;
        try {
            Usuario login = (Usuario) session.getAttribute("usuariosession");
            if (login == null || !login.getId().equals(id)) {
                return "redirect:/inicio";
            }
            usuario = usuarioService.buscarUsuario(id);
            UsuarioActualHolder.set(login.getId()); // <- unica linea nueva, sin interceptor ni WebConfig
            try {
                usuarioService.modificar(id, nombre, apellido, mail, clave2, clave2, archivo, idZona);
            } finally {
                UsuarioActualHolder.limpiar();
            }
            session.setAttribute("usuariosession", usuario);
            return "exito.html";

        } catch (ErrorServicio ex) {
            try {
                List<Zona> zonas = zonaService.listarZona();
                modelo.put("zonas", zonas);
            } catch (ErrorServicio e) {}

            modelo.put("error", ex.getMessage());
            modelo.put("perfil", usuario);
            return "perfil.html";

        }catch(Exception e) {
            e.printStackTrace();
            modelo.put("error", e.getMessage());
            return "perfil.html";
        }
    }

}
