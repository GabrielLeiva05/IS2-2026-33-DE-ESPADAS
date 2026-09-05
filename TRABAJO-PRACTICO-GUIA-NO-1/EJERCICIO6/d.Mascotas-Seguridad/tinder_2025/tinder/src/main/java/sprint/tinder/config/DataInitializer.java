package sprint.tinder.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import sprint.tinder.entities.Usuario;
import sprint.tinder.enumerations.Rol;
import sprint.tinder.repositories.UsuarioRepository;

import java.util.Date;

/**
 * Crea un usuario ADMIN de prueba la primera vez que se levanta la app,
 * para poder acceder al panel de administración sin tener que tocar la
 * base de datos a mano.
 *
 * Mail: admin@tinder.com
 * Clave: admin123
 *
 * IMPORTANTE: cambiar esta clave (o borrar este archivo) antes de llevar
 * el proyecto a un entorno real.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Usuario existente = usuarioRepository.buscarPorMail("admin@tinder.com");
        if (existente == null) {
            Usuario admin = new Usuario();
            admin.setNombre("Admin");
            admin.setApellido("Sistema");
            admin.setMail("admin@tinder.com");
            admin.setClave(passwordEncoder.encode("admin123"));
            admin.setRol(Rol.ADMIN);
            admin.setAlta(new Date());
            usuarioRepository.save(admin);
            System.out.println(">> Usuario ADMIN creado: admin@tinder.com / admin123");
        } else if (existente.getRol() == null || existente.getRol() != Rol.ADMIN) {
            // Por si ya existía de antes de agregar roles, lo dejamos como ADMIN igual.
            existente.setRol(Rol.ADMIN);
            usuarioRepository.save(existente);
        }
    }
}
