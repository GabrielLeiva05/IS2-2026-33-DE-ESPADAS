package com.competencia.blockazo.config;

import com.competencia.blockazo.model.entity.Usuario;
import com.competencia.blockazo.model.enums.Proveedor;
import com.competencia.blockazo.model.enums.Rol;
import com.competencia.blockazo.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Crea el usuario ADMIN la primera vez que se levanta la app,
 * para poder usar el ABM de usuarios sin tocar la base a mano.
 * (cambiar app.admin.clave en application.properties)
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String correoAdmin;
    private final String claveAdmin;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                           @Value("${app.admin.correo}") String correoAdmin,
                           @Value("${app.admin.clave}") String claveAdmin) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.correoAdmin = correoAdmin;
        this.claveAdmin = claveAdmin;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.buscarPorCorreo(correoAdmin).isPresent()) {
            return;
        }
        Usuario admin = Usuario.builder()
                .nombre("Admin")
                .apellido("Sistema")
                .correo(correoAdmin.toLowerCase())
                .clave(passwordEncoder.encode(claveAdmin))
                .rol(Rol.ADMIN)
                .proveedor(Proveedor.LOCAL)
                .fechaAlta(LocalDateTime.now())
                .build();
        admin.setEliminado(false);
        usuarioRepository.save(admin);
        log.info(">> Usuario ADMIN creado: {}", correoAdmin);
    }
}
