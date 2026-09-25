package sprint.tinder.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sprint.tinder.entities.Usuario;
import sprint.tinder.errors.ErrorServicio;
import sprint.tinder.services.UsuarioService;

import java.util.List;

// Todo lo que cuelga de /admin está restringido a ROLE_ADMIN en SecurityConfig,
// así que acá no hace falta volver a chequear el rol a mano.
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/usuarios")
    public String listarUsuarios(ModelMap model) {
        try {
            List<Usuario> usuarios = usuarioService.listarUsuario();
            model.put("usuarios", usuarios);
        } catch (ErrorServicio e) {
            model.put("error", e.getMessage());
        }
        return "admin-usuarios.html";
    }

    @PostMapping("/usuarios/deshabilitar")
    public String deshabilitar(@RequestParam String id) {
        try {
            usuarioService.deshabilitar(id);
        } catch (ErrorServicio ignored) {
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/habilitar")
    public String habilitar(@RequestParam String id) {
        try {
            usuarioService.habilitar(id);
        } catch (ErrorServicio ignored) {
        }
        return "redirect:/admin/usuarios";
    }
}
