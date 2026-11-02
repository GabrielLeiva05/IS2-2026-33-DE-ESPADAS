package com.dulcemayor.admin.service;

import com.dulcemayor.admin.cliente.ClienteApi;
import com.dulcemayor.admin.dto.EmpleadoFormularioDTO;
import com.dulcemayor.admin.dto.OpcionDTO;
import com.dulcemayor.admin.dto.OpcionesUbicacionDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/** Países, provincias, departamentos y localidades para los selects en cascada de las direcciones. */
@Service
public class ServicioUbicacion {

    private final ClienteApi clienteApi;

    public ServicioUbicacion(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public List<OpcionDTO> listarPaises(String token) {
        return clienteApi.listar(token, OpcionDTO[].class, "/api/paises");
    }

    public List<OpcionDTO> listarProvincias(String token, String idPais) {
        return clienteApi.listar(token, OpcionDTO[].class, "/api/provincias/pais/{id}", idPais);
    }

    public List<OpcionDTO> listarDepartamentos(String token, String idProvincia) {
        return clienteApi.listar(token, OpcionDTO[].class, "/api/departamentos/provincia/{id}", idProvincia);
    }

    public List<OpcionDTO> listarLocalidades(String token, String idDepartamento) {
        return clienteApi.listar(token, OpcionDTO[].class, "/api/localidades/departamento/{id}", idDepartamento);
    }

    /**
     * Opciones para dibujar el formulario: si ya hay valores elegidos (edición o reintento tras un error)
     * se cargan los niveles correspondientes; si hay un único país, se preselecciona.
     */
    public OpcionesUbicacionDTO opcionesPara(String token, EmpleadoFormularioDTO formulario) {
        List<OpcionDTO> paises = listarPaises(token);
        if (vacio(formulario.getIdPais()) && paises.size() == 1) {
            formulario.setIdPais(paises.get(0).getId());
        }
        List<OpcionDTO> provincias = vacio(formulario.getIdPais()) ? List.of()
                : listarProvincias(token, formulario.getIdPais());
        if (vacio(formulario.getIdProvincia()) && provincias.size() == 1) {
            formulario.setIdProvincia(provincias.get(0).getId());
        }
        List<OpcionDTO> departamentos = vacio(formulario.getIdProvincia()) ? List.of()
                : listarDepartamentos(token, formulario.getIdProvincia());
        List<OpcionDTO> localidades = vacio(formulario.getIdDepartamento()) ? List.of()
                : listarLocalidades(token, formulario.getIdDepartamento());
        return new OpcionesUbicacionDTO(paises, provincias, departamentos, localidades);
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
