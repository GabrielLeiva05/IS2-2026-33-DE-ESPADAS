package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Persona;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

/**
 * Reglas de negocio comunes a toda Persona (empleados y clientes), en la capa de servicio.
 * Se toman de UsuarioService de Blockazo (nombre solo con letras, DNI de 7 u 8 dígitos)
 * y se agregan las del diagrama (tipos de documento CUIL, CUIT y pasaporte).
 */
final class ValidacionesPersona {

    /** Solo letras (con acentos y ñ) separadas por espacio, apóstrofo o guion: "María José", "O'Brien". */
    static final Pattern NOMBRE = Pattern.compile("^[\\p{L}\\p{M}]+(?:[ '-][\\p{L}\\p{M}]+)*$");
    private static final Pattern DNI = Pattern.compile("\\d{7,8}");
    private static final Pattern CUIL_CUIT = Pattern.compile("\\d{11}");
    private static final Pattern PASAPORTE = Pattern.compile("[A-Z0-9]{6,9}");

    private ValidacionesPersona() {
    }

    static void validarNombreYApellido(Persona persona) throws MiException {
        if (persona.getNombre() == null || persona.getNombre().isBlank()) {
            throw new MiException("Debe indicar el nombre");
        }
        if (persona.getApellido() == null || persona.getApellido().isBlank()) {
            throw new MiException("Debe indicar el apellido");
        }
        if (!NOMBRE.matcher(persona.getNombre()).matches()) {
            throw new MiException("El nombre solo puede contener letras");
        }
        if (!NOMBRE.matcher(persona.getApellido()).matches()) {
            throw new MiException("El apellido solo puede contener letras");
        }
    }

    /** Valida el formato del documento según su tipo. Ambos datos deben venir juntos. */
    static void validarDocumento(TipoDocumento tipo, String numero, boolean obligatorio) throws MiException {
        boolean sinTipo = tipo == null;
        boolean sinNumero = numero == null || numero.isBlank();
        if (sinTipo && sinNumero) {
            if (obligatorio) {
                throw new MiException("Debe indicar el tipo y el número de documento");
            }
            return;
        }
        if (sinTipo || sinNumero) {
            throw new MiException("Debe indicar juntos el tipo y el número de documento");
        }
        boolean valido = switch (tipo) {
            case DNI -> DNI.matcher(numero).matches();
            case CUIL, CUIT -> CUIL_CUIT.matcher(numero).matches();
            case PASAPORTE -> PASAPORTE.matcher(numero).matches();
        };
        if (!valido) {
            throw new MiException(switch (tipo) {
                case DNI -> "El DNI debe tener 7 u 8 dígitos";
                case CUIL, CUIT -> "El " + tipo + " debe tener 11 dígitos, sin guiones";
                case PASAPORTE -> "El pasaporte debe tener entre 6 y 9 letras o números";
            });
        }
    }

    /** Fecha de nacimiento en el pasado y persona mayor de edad. */
    static void validarMayorDeEdad(LocalDate fechaNacimiento, boolean obligatoria) throws MiException {
        if (fechaNacimiento == null) {
            if (obligatoria) {
                throw new MiException("Debe indicar la fecha de nacimiento");
            }
            return;
        }
        if (!fechaNacimiento.isBefore(LocalDate.now())) {
            throw new MiException("La fecha de nacimiento debe ser anterior a hoy");
        }
        if (Period.between(fechaNacimiento, LocalDate.now()).getYears() < 18) {
            throw new MiException("La persona debe ser mayor de edad");
        }
    }

    /** Quita puntos, guiones y espacios del número de documento y lo pasa a mayúsculas. */
    static String normalizarDocumento(String numero) {
        if (numero == null) {
            return null;
        }
        String limpio = numero.replace(".", "").replace("-", "").replace(" ", "").toUpperCase();
        return limpio.isEmpty() ? null : limpio;
    }

    /**
     * Los nombres que llegan de Google o Facebook pueden traer números o símbolos.
     * Se limpian para cumplir la regla de nombre; si no queda nada válido, se usa el valor por defecto.
     * (Reutilizado de Blockazo.)
     */
    static String nombreValido(String valor, String porDefecto) {
        if (valor == null || valor.isBlank()) {
            return porDefecto;
        }
        String limpio = valor.replaceAll("[^\\p{L}\\p{M} '-]", "").replaceAll("\\s+", " ").trim();
        return NOMBRE.matcher(limpio).matches() ? limpio : porDefecto;
    }
}
