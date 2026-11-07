package com.ejercicioIntegrador.dulcemayor.config;

import com.ejercicioIntegrador.dulcemayor.dto.DepartamentoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.DepartamentoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.DireccionRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.EmpleadoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.LocalidadRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.LocalidadResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PaisRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PaisResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ProvinciaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ProvinciaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioDepartamento;
import com.ejercicioIntegrador.dulcemayor.service.ServicioEmpleado;
import com.ejercicioIntegrador.dulcemayor.service.ServicioLocalidad;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPais;
import com.ejercicioIntegrador.dulcemayor.service.ServicioProvincia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Datos iniciales, cargados SIEMPRE a través de los servicios (pasan por las mismas validaciones):
 *  1) Argentina > Mendoza > sus 18 departamentos, con la localidad cabecera de cada uno.
 *     Solo se informa el código postal de la Ciudad de Mendoza (5500, dato del Integrador 1);
 *     el resto se completa desde la API.
 *  2) El jefe administrativo inicial, con la clave temporal "dulcemayor": al ingresar debe cambiarla.
 * Es idempotente: si los datos ya existen, no los vuelve a crear.
 */
@Component
@Order(1)
public class InicializadorDatos implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(InicializadorDatos.class);

    /** Departamento -> localidad cabecera. */
    private static final Map<String, String> DEPARTAMENTOS_MENDOZA = new LinkedHashMap<>();

    static {
        DEPARTAMENTOS_MENDOZA.put("Capital", "Ciudad de Mendoza");
        DEPARTAMENTOS_MENDOZA.put("Godoy Cruz", "Godoy Cruz");
        DEPARTAMENTOS_MENDOZA.put("Guaymallén", "Villa Nueva");
        DEPARTAMENTOS_MENDOZA.put("Las Heras", "Las Heras");
        DEPARTAMENTOS_MENDOZA.put("Luján de Cuyo", "Luján de Cuyo");
        DEPARTAMENTOS_MENDOZA.put("Maipú", "Maipú");
        DEPARTAMENTOS_MENDOZA.put("Lavalle", "Villa Tulumaya");
        DEPARTAMENTOS_MENDOZA.put("San Martín", "San Martín");
        DEPARTAMENTOS_MENDOZA.put("Junín", "Junín");
        DEPARTAMENTOS_MENDOZA.put("Rivadavia", "Rivadavia");
        DEPARTAMENTOS_MENDOZA.put("Santa Rosa", "Santa Rosa");
        DEPARTAMENTOS_MENDOZA.put("La Paz", "La Paz");
        DEPARTAMENTOS_MENDOZA.put("San Rafael", "San Rafael");
        DEPARTAMENTOS_MENDOZA.put("General Alvear", "General Alvear");
        DEPARTAMENTOS_MENDOZA.put("Malargüe", "Malargüe");
        DEPARTAMENTOS_MENDOZA.put("Tunuyán", "Tunuyán");
        DEPARTAMENTOS_MENDOZA.put("Tupungato", "Tupungato");
        DEPARTAMENTOS_MENDOZA.put("San Carlos", "San Carlos");
    }

    private final ServicioPais servicioPais;
    private final ServicioProvincia servicioProvincia;
    private final ServicioDepartamento servicioDepartamento;
    private final ServicioLocalidad servicioLocalidad;
    private final ServicioEmpleado servicioEmpleado;

    @Value("${app.jefe-inicial.correo}")
    private String correoJefe;
    @Value("${app.jefe-inicial.nombre}")
    private String nombreJefe;
    @Value("${app.jefe-inicial.apellido}")
    private String apellidoJefe;
    @Value("${app.jefe-inicial.dni}")
    private String dniJefe;

    public InicializadorDatos(ServicioPais servicioPais, ServicioProvincia servicioProvincia,
                              ServicioDepartamento servicioDepartamento, ServicioLocalidad servicioLocalidad,
                              ServicioEmpleado servicioEmpleado) {
        this.servicioPais = servicioPais;
        this.servicioProvincia = servicioProvincia;
        this.servicioDepartamento = servicioDepartamento;
        this.servicioLocalidad = servicioLocalidad;
        this.servicioEmpleado = servicioEmpleado;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            String idCiudadDeMendoza = cargarUbicacionDeMendoza();
            crearJefeInicial(idCiudadDeMendoza);
        } catch (MiException e) {
            LOG.error("No se pudieron cargar los datos iniciales: {}", e.getMessage());
        }
    }

    private String cargarUbicacionDeMendoza() throws MiException {
        PaisResponseDTO argentina = buscarOCrearPais("Argentina");
        ProvinciaResponseDTO mendoza = buscarOCrearProvincia("Mendoza", argentina.id());
        String idCiudadDeMendoza = null;
        for (Map.Entry<String, String> entrada : DEPARTAMENTOS_MENDOZA.entrySet()) {
            DepartamentoResponseDTO departamento = buscarOCrearDepartamento(entrada.getKey(), mendoza.id());
            String codigoPostal = "Ciudad de Mendoza".equals(entrada.getValue()) ? "5500" : null;
            LocalidadResponseDTO localidad = buscarOCrearLocalidad(entrada.getValue(), codigoPostal, departamento.id());
            if ("Ciudad de Mendoza".equals(localidad.nombre())) {
                idCiudadDeMendoza = localidad.id();
            }
        }
        return idCiudadDeMendoza;
    }

    private void crearJefeInicial(String idLocalidad) throws MiException {
        if (servicioEmpleado.existeJefeActivo()) {
            return;
        }
        servicioEmpleado.alta(new EmpleadoRequestDTO(nombreJefe, apellidoJefe, LocalDate.of(1980, 1, 1),
                TipoDocumento.DNI, dniJefe, correoJefe, RolUsuario.JEFE, null, null,
                new DireccionRequestDTO("Av. San Martín", "1000", null, null, null,
                        "Dirección inicial: actualizarla desde el sistema", null, null, idLocalidad),
                null, null, null, null));
        LOG.info("Jefe administrativo inicial creado: {} (clave temporal: debe cambiarla al ingresar)", correoJefe);
    }

    private PaisResponseDTO buscarOCrearPais(String nombre) throws MiException {
        try {
            return servicioPais.buscarPaisPorNombre(nombre);
        } catch (RecursoNoEncontradoException e) {
            return servicioPais.alta(new PaisRequestDTO(nombre));
        }
    }

    private ProvinciaResponseDTO buscarOCrearProvincia(String nombre, String idPais) throws MiException {
        for (ProvinciaResponseDTO provincia : servicioProvincia.listarProvinciaActivo(idPais)) {
            if (provincia.nombre().equalsIgnoreCase(nombre)) {
                return provincia;
            }
        }
        return servicioProvincia.alta(new ProvinciaRequestDTO(nombre, idPais));
    }

    private DepartamentoResponseDTO buscarOCrearDepartamento(String nombre, String idProvincia) throws MiException {
        for (DepartamentoResponseDTO departamento : servicioDepartamento.listarDepartamentoActivo(idProvincia)) {
            if (departamento.nombre().equalsIgnoreCase(nombre)) {
                return departamento;
            }
        }
        return servicioDepartamento.alta(new DepartamentoRequestDTO(nombre, idProvincia));
    }

    private LocalidadResponseDTO buscarOCrearLocalidad(String nombre, String codigoPostal, String idDepartamento)
            throws MiException {
        for (LocalidadResponseDTO localidad : servicioLocalidad.listarLocalidadActivo(idDepartamento)) {
            if (localidad.nombre().equalsIgnoreCase(nombre)) {
                return localidad;
            }
        }
        return servicioLocalidad.alta(new LocalidadRequestDTO(nombre, codigoPostal, idDepartamento));
    }
}
