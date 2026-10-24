package com.uncuyo.tp1_ej4.config;

import com.uncuyo.tp1_ej4.entities.Rol;
import com.uncuyo.tp1_ej4.entities.Usuario;
import com.uncuyo.tp1_ej4.repositories.RepositorioRol;
import com.uncuyo.tp1_ej4.repositories.RepositorioUsuario;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Crea los roles (ADMIN, USER) y un usuario de cada uno al arrancar la
 * aplicacion, PERO SOLO SI todavia no existen. Esto es necesario porque la
 * base no tiene ningun usuario cargado y, sin al menos un ADMIN, nadie
 * podria entrar al panel de administracion la primera vez.
 *
 * IMPORTANTE (seguridad): "admin123" y "user123" son credenciales de
 * arranque para desarrollo/pruebas, nunca para produccion. Antes de
 * desplegar el proyecto en serio:
 *   1) Cambiar estas contrasenas (entrando y actualizandolas, o
 *      modificando el valor aca antes del primer arranque).
 *   2) Considerar eliminar este seeder una vez que ya haya usuarios reales
 *      cargados, para que no se vuelva a ejecutar en cada arranque
 *      (aunque tal cual esta, no hace nada si ya existe al menos un
 *      usuario).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final RepositorioUsuario repoUsuario;
    private final RepositorioRol repoRol;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RepositorioUsuario repoUsuario, RepositorioRol repoRol, PasswordEncoder passwordEncoder) {
        this.repoUsuario = repoUsuario;
        this.repoRol = repoRol;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Rol rolAdmin = repoRol.findByNombre("ADMIN").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombre("ADMIN");
            return repoRol.save(r);
        });

        Rol rolUser = repoRol.findByNombre("USER").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombre("USER");
            return repoRol.save(r);
        });

        if (repoUsuario.count() == 0) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setActivo(true);
            Set<Rol> rolesAdmin = new HashSet<>();
            rolesAdmin.add(rolAdmin);
            admin.setRoles(rolesAdmin);
            repoUsuario.save(admin);

            Usuario user = new Usuario();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setActivo(true);
            Set<Rol> rolesUser = new HashSet<>();
            rolesUser.add(rolUser);
            user.setRoles(rolesUser);
            repoUsuario.save(user);
        }
    }
}
