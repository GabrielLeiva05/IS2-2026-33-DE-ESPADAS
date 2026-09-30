package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.dto.ActualizarPerfilDTO;
import com.ejercicioIntegrador.tiendaderopa.dto.PerfilDTO;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.ContactoTelefonico;
import com.ejercicioIntegrador.tiendaderopa.model.Direccion;
import com.ejercicioIntegrador.tiendaderopa.model.Localidad;
import com.ejercicioIntegrador.tiendaderopa.model.Persona;
import com.ejercicioIntegrador.tiendaderopa.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Date;

@Service
public class PerfilServicio {

    private final UsuarioServicio usuarioServicio;
    private final PersonaServicio personaServicio;
    private final DireccionServicio direccionServicio;
    private final LocalidadServicio localidadServicio;
    private final ServicioContactoTelefonico contactoTelefonicoServicio;

    public PerfilServicio(UsuarioServicio usuarioServicio, PersonaServicio personaServicio,
            DireccionServicio direccionServicio, LocalidadServicio localidadServicio,
            ServicioContactoTelefonico contactoTelefonicoServicio) {
        this.usuarioServicio = usuarioServicio;
        this.personaServicio = personaServicio;
        this.direccionServicio = direccionServicio;
        this.localidadServicio = localidadServicio;
        this.contactoTelefonicoServicio = contactoTelefonicoServicio;
    }

    @Transactional(readOnly = true)
    public PerfilDTO obtener(String email) throws MiException {
        return aDTO(usuarioServicio.buscarActivoPorNombreUsuario(email));
    }

    @Transactional
    public PerfilDTO actualizar(String email, ActualizarPerfilDTO datos) throws MiException {
        Usuario usuario = usuarioServicio.buscarActivoPorNombreUsuario(email);
        Persona persona = usuario.getPersona();
        if (persona == null) {
            throw new MiException("La cuenta no tiene datos personales asociados");
        }

        persona.setNombre(datos.nombre().trim());
        persona.setApellido(datos.apellido().trim());
        persona.setSexo(datos.sexo() == null ? null : datos.sexo().trim());
        persona.setFechaNacimiento(Date.from(datos.fechaNacimiento()
                .atStartOfDay(ZoneId.systemDefault()).toInstant()));

        Localidad localidad = localidadServicio.buscarPorId(datos.localidadId());
        direccionServicio.guardarDireccionPerfil(persona, localidad, datos.codigoPostal(), datos.barrio(),
                datos.calle(), datos.numeracion(), datos.manzanaPiso(), datos.casaDepartamento(), datos.referencia());
        contactoTelefonicoServicio.guardarTelefonoPerfil(persona, datos.telefono());
        personaServicio.guardarCambiosPerfil(persona);

        return aDTO(usuario);
    }

    private PerfilDTO aDTO(Usuario usuario) throws MiException {
        Persona persona = usuario.getPersona();
        if (persona == null) {
            throw new MiException("La cuenta no tiene datos personales asociados");
        }

        Direccion direccion = persona.getDirecciones().stream()
                .filter(actual -> !actual.isEliminado())
                .findFirst()
                .orElse(null);
        ContactoTelefonico telefono = persona.getContactos().stream()
                .filter(ContactoTelefonico.class::isInstance)
                .map(ContactoTelefonico.class::cast)
                .filter(actual -> !actual.isEliminado())
                .findFirst()
                .orElse(null);
        Localidad localidad = direccion == null ? null : direccion.getLocalidad();

        return new PerfilDTO(
                usuario.getNombreUsuario(), persona.getNombre(), persona.getApellido(), persona.getSexo(),
                persona.getFechaNacimiento().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                persona.getTipoDocumento().name(), persona.getDocumento(),
                telefono == null ? null : telefono.getTelefono(),
                direccion == null ? null : direccion.getCodigoPostal(),
                direccion == null ? null : direccion.getBarrio(),
                direccion == null ? null : direccion.getCalle(),
                direccion == null ? null : direccion.getNumeracion(),
                direccion == null ? null : direccion.getManzanaPiso(),
                direccion == null ? null : direccion.getCasaDepartamento(),
                direccion == null ? null : direccion.getReferencia(),
                localidad == null ? null : localidad.getId(),
                localidad == null ? null : localidad.getNombre(),
                localidad == null ? null : localidad.getDepartamento().getNombre(),
                localidad == null ? null : localidad.getDepartamento().getProvincia().getNombre(),
                localidad == null ? null : localidad.getDepartamento().getProvincia().getPais().getNombre());
    }
}