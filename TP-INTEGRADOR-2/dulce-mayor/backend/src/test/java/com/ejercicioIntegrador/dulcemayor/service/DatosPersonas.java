package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.DireccionRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.EmpleadoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.ProveedorAutenticacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.dulcemayor.model.Direccion;
import com.ejercicioIntegrador.dulcemayor.model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

/** Objetos de prueba para personas, cuentas y direcciones. */
final class DatosPersonas {

    private DatosPersonas() {
    }

    /** Codificador de prueba: deja ver si la clave se transformó antes de guardarse. */
    static final PasswordEncoder CODIFICADOR = new PasswordEncoder() {
        @Override
        public String encode(CharSequence clave) {
            return "HASH(" + clave + ")";
        }

        @Override
        public boolean matches(CharSequence clave, String codificada) {
            return codificada != null && codificada.equals("HASH(" + clave + ")");
        }
    };

    static Usuario usuario(String id, String correo, RolUsuario rol, ProveedorAutenticacion proveedor, String claveHash) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombreUsuario(correo);
        usuario.setRol(rol);
        usuario.setProveedorAutenticacion(proveedor);
        usuario.setClave(claveHash);
        return usuario;
    }

    static DireccionRequestDTO direccion() {
        return new DireccionRequestDTO("San Martín", "1234", null, null, null, null, null, null, "loc-1");
    }

    static Direccion direccionGuardada() {
        Direccion direccion = new Direccion();
        direccion.setId("dir-1");
        direccion.setCalle("San Martín");
        direccion.setNumeracion("1234");
        return direccion;
    }

    static EmpleadoRequestDTO empleado(RolUsuario rol, String correo) {
        return new EmpleadoRequestDTO("Ana", "Pérez", LocalDate.of(1990, 5, 20), TipoDocumento.DNI, "30.111.222",
                correo, rol, null, null, direccion(), "Zona Este", "Fiat Fiorino", "AB 123 CD", null);
    }
}
