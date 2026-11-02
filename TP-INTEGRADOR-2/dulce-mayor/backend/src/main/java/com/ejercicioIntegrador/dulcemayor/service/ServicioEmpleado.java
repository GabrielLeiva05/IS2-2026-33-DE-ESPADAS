package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.EmpleadoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.EmpleadoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.ProveedorAutenticacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoImagen;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.OperacionNoPermitidaException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Distribuidor;
import com.ejercicioIntegrador.dulcemayor.model.Empleado;
import com.ejercicioIntegrador.dulcemayor.model.Preventista;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioEmpleado;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseUseCaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Alta de empleados (HU-07). El alta crea, dentro de la misma transacción:
 * la dirección (ServicioDireccion), la cuenta con la clave temporal (ServicioUsuario) y el empleado.
 * Si cualquier validación falla se revierte todo.
 *
 * Según el rol se instancia la subclase del diagrama: PREVENTISTA -> Preventista, DISTRIBUIDOR -> Distribuidor,
 * ADMINISTRATIVO y JEFE -> Empleado. Desde el sistema no se pueden crear jefes: solo existe el jefe inicial.
 */
@Service
public class ServicioEmpleado extends BaseService<Empleado, EmpleadoRequestDTO, EmpleadoResponseDTO> {

    private static final Pattern LEGAJO = Pattern.compile("^[A-Z0-9-]{1,20}$");
    /** Patentes argentinas: formato viejo ABC123 o Mercosur AB123CD. */
    private static final Pattern PATENTE = Pattern.compile("^([A-Z]{3}\\d{3}|[A-Z]{2}\\d{3}[A-Z]{2})$");

    private final RepositorioEmpleado repositorioEmpleado;
    private final ServicioUsuario servicioUsuario;
    private final ServicioDireccion servicioDireccion;
    private final ServicioImagen servicioImagen;

    public ServicioEmpleado(RepositorioEmpleado repositorioEmpleado, ServicioUsuario servicioUsuario,
                            ServicioDireccion servicioDireccion, ServicioImagen servicioImagen) {
        super(repositorioEmpleado);
        this.repositorioEmpleado = repositorioEmpleado;
        this.servicioUsuario = servicioUsuario;
        this.servicioDireccion = servicioDireccion;
        this.servicioImagen = servicioImagen;
    }

    // ------------------------------------------------------------------
    // Pasos del Template Method
    // ------------------------------------------------------------------

    @Override
    protected Empleado crearEntidad(EmpleadoRequestDTO dto) throws MiException {
        RolUsuario rol = dto.rol();
        if (rol == null) {
            throw new MiException("Debe indicar el rol del empleado");
        }
        if (rol == RolUsuario.CLIENTE) {
            throw new MiException("El rol CLIENTE no corresponde a un empleado");
        }
        Empleado empleado = switch (rol) {
            case PREVENTISTA -> new Preventista();
            case DISTRIBUIDOR -> new Distribuidor();
            default -> new Empleado();
        };
        copiarDatos(empleado, dto);
        empleado.setLegajo(estaVacio(dto.legajo()) ? generarLegajo() : normalizarLegajo(dto.legajo()));
        empleado.setFechaIngreso(dto.fechaIngreso() == null ? LocalDate.now() : dto.fechaIngreso());
        if (dto.direccion() == null) {
            throw new MiException("Debe indicar la dirección del empleado");
        }
        empleado.setDireccion(servicioDireccion.crearDireccion(dto.direccion()));
        empleado.setUsuario(servicioUsuario.crearUsuario(dto.correo(), rol, null, ProveedorAutenticacion.LOCAL));
        return empleado;
    }

    @Override
    protected void actualizarEntidad(Empleado empleado, EmpleadoRequestDTO dto) throws MiException {
        if (dto.rol() != null && dto.rol() != empleado.getUsuario().getRol()) {
            throw new MiException("El rol de un empleado no se puede modificar");
        }
        if (!estaVacio(dto.correo())
                && !ServicioUsuario.normalizarCorreo(dto.correo()).equals(empleado.getUsuario().getNombreUsuario())) {
            throw new MiException("El correo no se puede modificar: es el usuario de ingreso");
        }
        copiarDatos(empleado, dto);
        if (!estaVacio(dto.legajo())) {
            empleado.setLegajo(normalizarLegajo(dto.legajo()));
        }
        if (dto.fechaIngreso() != null) {
            empleado.setFechaIngreso(dto.fechaIngreso());
        }
        if (dto.direccion() != null) {
            if (empleado.getDireccion() == null) {
                empleado.setDireccion(servicioDireccion.crearDireccion(dto.direccion()));
            } else {
                servicioDireccion.modificar(empleado.getDireccion().getId(), dto.direccion());
            }
        }
    }

    @Override
    protected void validar(BaseUseCaseService casoDeUso, Empleado empleado) throws MiException {
        RolUsuario rol = empleado.getUsuario() == null ? null : empleado.getUsuario().getRol();
        if (casoDeUso == BaseUseCaseService.BAJA) {
            if (rol == RolUsuario.JEFE) {
                throw new OperacionNoPermitidaException("El jefe administrativo no se puede dar de baja desde el sistema");
            }
            return;
        }
        if (rol == null) {
            throw new MiException("El empleado debe tener una cuenta de usuario");
        }
        if (casoDeUso == BaseUseCaseService.ALTA && rol == RolUsuario.JEFE
                && repositorioEmpleado.existsByUsuario_RolAndEliminadoFalse(RolUsuario.JEFE)) {
            throw new MiException("Ya existe un jefe administrativo: desde el sistema solo se pueden dar de alta "
                    + "administrativos, preventistas y distribuidores");
        }
        ValidacionesPersona.validarNombreYApellido(empleado);
        ValidacionesPersona.validarDocumento(empleado.getTipoDocumento(), empleado.getNumeroDocumento(), true);
        ValidacionesPersona.validarMayorDeEdad(empleado.getFechaNacimiento(), true);
        if (empleado.getFechaIngreso() == null || empleado.getFechaIngreso().isAfter(LocalDate.now())) {
            throw new MiException("La fecha de ingreso no puede ser posterior a hoy");
        }
        if (estaVacio(empleado.getLegajo()) || !LEGAJO.matcher(empleado.getLegajo()).matches()) {
            throw new MiException("El legajo solo puede tener letras, números y guiones (máximo 20)");
        }
        if (empleado.getDireccion() == null) {
            throw new MiException("Debe indicar la dirección del empleado");
        }
        if (empleado.getImagen() != null && empleado.getImagen().getTipoImagen() != TipoImagen.PERSONA) {
            throw new MiException("La foto del empleado debe ser una imagen de tipo PERSONA");
        }
        if (empleado instanceof Preventista preventista && estaVacio(preventista.getZona())) {
            throw new MiException("Debe indicar la zona del preventista");
        }
        if (empleado instanceof Distribuidor distribuidor) {
            if (estaVacio(distribuidor.getVehiculo())) {
                throw new MiException("Debe indicar el vehículo del distribuidor");
            }
            if (estaVacio(distribuidor.getPatente()) || !PATENTE.matcher(distribuidor.getPatente()).matches()) {
                throw new MiException("La patente debe tener formato ABC123 o AB123CD");
            }
        }
        Optional<Empleado> mismoDocumento = repositorioEmpleado.findFirstByTipoDocumentoAndNumeroDocumentoAndEliminadoFalse(
                empleado.getTipoDocumento(), empleado.getNumeroDocumento());
        if (mismoDocumento.isPresent() && !Objects.equals(mismoDocumento.get().getId(), empleado.getId())) {
            throw new MiException("Ya existe un empleado con el documento " + empleado.getNumeroDocumento());
        }
        Optional<Empleado> mismoLegajo = repositorioEmpleado.findFirstByLegajoIgnoreCaseAndEliminadoFalse(empleado.getLegajo());
        if (mismoLegajo.isPresent() && !Objects.equals(mismoLegajo.get().getId(), empleado.getId())) {
            throw new MiException("Ya existe un empleado con el legajo " + empleado.getLegajo());
        }
    }

    /** Al dar de baja al empleado también se desactiva su cuenta: ya no puede iniciar sesión. */
    @Override
    protected void postBaja(Empleado empleado) throws MiException {
        if (empleado.getUsuario() != null && !empleado.getUsuario().isEliminado()) {
            servicioUsuario.baja(empleado.getUsuario().getId());
        }
    }

    @Override
    public EmpleadoResponseDTO convertirAResponse(Empleado empleado) {
        String zona = empleado instanceof Preventista preventista ? preventista.getZona() : null;
        String vehiculo = empleado instanceof Distribuidor distribuidor ? distribuidor.getVehiculo() : null;
        String patente = empleado instanceof Distribuidor distribuidor ? distribuidor.getPatente() : null;
        return new EmpleadoResponseDTO(empleado.getId(), empleado.getNombre(), empleado.getApellido(),
                empleado.getFechaNacimiento(), empleado.getTipoDocumento(), empleado.getNumeroDocumento(),
                empleado.getUsuario().getNombreUsuario(), empleado.getUsuario().getRol(),
                empleado.getUsuario().isDebeCambiarClave(), empleado.getLegajo(), empleado.getFechaIngreso(),
                empleado.getDireccion() == null ? null : servicioDireccion.convertirAResponse(empleado.getDireccion()),
                zona, vehiculo, patente,
                empleado.getImagen() == null ? null : empleado.getImagen().getId(),
                empleado.isEliminado());
    }

    @Override
    protected String nombreEntidad() {
        return "el empleado";
    }

    // ------------------------------------------------------------------
    // Operaciones propias (diagrama de clases)
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public EmpleadoResponseDTO buscarEmpleadoPorLegajo(String legajo) throws MiException {
        if (estaVacio(legajo)) {
            throw new MiException("Debe indicar el legajo a buscar");
        }
        return repositorioEmpleado.findFirstByLegajoIgnoreCaseAndEliminadoFalse(legajo.trim())
                .map(this::convertirAResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un empleado con el legajo " + legajo));
    }

    /** Cubre listarPreventista, listarDistribuidor y los administrativos: el rol identifica la subclase. */
    @Transactional(readOnly = true)
    public List<EmpleadoResponseDTO> listarPorRol(RolUsuario rol) throws MiException {
        if (rol == null || rol == RolUsuario.CLIENTE) {
            throw new MiException("Debe indicar un rol de empleado");
        }
        return convertirAResponse(repositorioEmpleado.findByUsuario_RolAndEliminadoFalseOrderByApellidoAscNombreAsc(rol));
    }

    /** Empleado dueño de una cuenta. Lo usa la autenticación para obtener el nombre del usuario. */
    @Transactional(readOnly = true)
    public Optional<Empleado> buscarPorUsuario(String idUsuario) {
        return repositorioEmpleado.findFirstByUsuario_Id(idUsuario);
    }

    @Transactional(readOnly = true)
    public boolean existeJefeActivo() {
        return repositorioEmpleado.existsByUsuario_RolAndEliminadoFalse(RolUsuario.JEFE);
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private void copiarDatos(Empleado empleado, EmpleadoRequestDTO dto) throws MiException {
        empleado.setNombre(normalizarTexto(dto.nombre()));
        empleado.setApellido(normalizarTexto(dto.apellido()));
        empleado.setFechaNacimiento(dto.fechaNacimiento());
        empleado.setTipoDocumento(dto.tipoDocumento());
        empleado.setNumeroDocumento(ValidacionesPersona.normalizarDocumento(dto.numeroDocumento()));
        if (dto.idImagen() != null) {
            empleado.setImagen(estaVacio(dto.idImagen()) ? null
                    : obtenerReferencia(servicioImagen, dto.idImagen(), "la imagen"));
        }
        if (empleado instanceof Preventista preventista) {
            preventista.setZona(normalizarTexto(dto.zona()));
        }
        if (empleado instanceof Distribuidor distribuidor) {
            distribuidor.setVehiculo(normalizarTexto(dto.vehiculo()));
            distribuidor.setPatente(dto.patente() == null ? null
                    : dto.patente().replace(" ", "").replace("-", "").toUpperCase(Locale.ROOT));
        }
    }

    /** Legajo correlativo EMP-00001, EMP-00002... que no se haya usado antes (ni por empleados dados de baja). */
    private String generarLegajo() {
        long numero = repositorioEmpleado.count() + 1;
        String legajo = String.format("EMP-%05d", numero);
        while (repositorioEmpleado.existsByLegajoIgnoreCase(legajo)) {
            numero++;
            legajo = String.format("EMP-%05d", numero);
        }
        return legajo;
    }

    private static String normalizarLegajo(String legajo) {
        return legajo.trim().toUpperCase(Locale.ROOT);
    }
}
