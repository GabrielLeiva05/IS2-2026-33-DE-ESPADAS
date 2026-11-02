package com.dulcemayor.admin.service;

import com.dulcemayor.admin.cliente.ClienteApi;
import com.dulcemayor.admin.dto.DireccionSolicitudDTO;
import com.dulcemayor.admin.dto.EmpleadoDTO;
import com.dulcemayor.admin.dto.EmpleadoFormularioDTO;
import com.dulcemayor.admin.dto.EmpleadoSolicitudDTO;
import com.dulcemayor.admin.dto.Roles;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Gestión de empleados (HU-07) a través de /api/empleados. Las validaciones (obligatorios, duplicados,
 * edad, documento, rol asignable, clave temporal) las aplica la API: aquí solo se arma la solicitud.
 */
@Service
public class ServicioEmpleados {

    private final ClienteApi clienteApi;

    public ServicioEmpleados(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public List<EmpleadoDTO> listar(String token, String rol) {
        List<EmpleadoDTO> empleados = vacio(rol)
                ? clienteApi.listar(token, EmpleadoDTO[].class, "/api/empleados")
                : clienteApi.listar(token, EmpleadoDTO[].class, "/api/empleados/rol/{rol}", rol);
        return empleados.stream()
                .sorted(Comparator.comparing(EmpleadoDTO::getApellido, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(EmpleadoDTO::getNombre, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    public EmpleadoDTO consultar(String token, String id) {
        return clienteApi.obtener(token, EmpleadoDTO.class, "/api/empleados/{id}", id);
    }

    public EmpleadoDTO crear(String token, EmpleadoFormularioDTO formulario) {
        return clienteApi.enviar(token, aSolicitud(formulario), EmpleadoDTO.class, "/api/empleados");
    }

    public EmpleadoDTO modificar(String token, String id, EmpleadoFormularioDTO formulario) {
        return clienteApi.reemplazar(token, aSolicitud(formulario), EmpleadoDTO.class, "/api/empleados/{id}", id);
    }

    public void darDeBaja(String token, String id) {
        clienteApi.eliminar(token, "/api/empleados/{id}", id);
    }

    public List<String> rolesAsignables() {
        return Roles.ASIGNABLES;
    }

    private static EmpleadoSolicitudDTO aSolicitud(EmpleadoFormularioDTO f) {
        DireccionSolicitudDTO direccion = new DireccionSolicitudDTO(texto(f.getCalle()), texto(f.getNumeracion()),
                texto(f.getBarrio()), texto(f.getManzanaPiso()), texto(f.getCasaDepartamento()),
                texto(f.getReferencia()), null, null, texto(f.getIdLocalidad()));
        boolean preventista = Roles.PREVENTISTA.equals(f.getRol());
        boolean distribuidor = Roles.DISTRIBUIDOR.equals(f.getRol());
        return new EmpleadoSolicitudDTO(texto(f.getNombre()), texto(f.getApellido()), texto(f.getFechaNacimiento()),
                texto(f.getTipoDocumento()), texto(f.getNumeroDocumento()), texto(f.getCorreo()), texto(f.getRol()),
                texto(f.getLegajo()), texto(f.getFechaIngreso()), direccion,
                preventista ? texto(f.getZona()) : null,
                distribuidor ? texto(f.getVehiculo()) : null,
                distribuidor ? texto(f.getPatente()) : null,
                null);
    }

    /** Los campos vacíos viajan como null para que la API aplique sus reglas de obligatoriedad. */
    private static String texto(String valor) {
        return vacio(valor) ? null : valor.trim();
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
