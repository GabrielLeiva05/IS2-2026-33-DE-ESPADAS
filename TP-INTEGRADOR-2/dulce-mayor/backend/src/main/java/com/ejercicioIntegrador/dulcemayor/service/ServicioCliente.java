package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.ClienteCampoDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ClienteRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ClienteResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.DatosPersonalesDTO;
import com.ejercicioIntegrador.dulcemayor.dto.RegistroClienteDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.ProveedorAutenticacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Cliente;
import com.ejercicioIntegrador.dulcemayor.model.Usuario;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioCliente;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseUseCaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Clientes minoristas (HU-09). El registro crea el cliente y su cuenta con rol CLIENTE en una sola transacción.
 * El rol nunca llega del exterior: lo fija este servicio.
 */
@Service
public class ServicioCliente extends BaseService<Cliente, ClienteRequestDTO, ClienteResponseDTO> {

    private final RepositorioCliente repositorioCliente;
    private final ServicioUsuario servicioUsuario;
    private final ServicioDireccion servicioDireccion;

    public ServicioCliente(RepositorioCliente repositorioCliente, ServicioUsuario servicioUsuario,
                           ServicioDireccion servicioDireccion) {
        super(repositorioCliente);
        this.repositorioCliente = repositorioCliente;
        this.servicioUsuario = servicioUsuario;
        this.servicioDireccion = servicioDireccion;
    }

    // ------------------------------------------------------------------
    // Pasos del Template Method
    // ------------------------------------------------------------------

    @Override
    protected Cliente crearEntidad(ClienteRequestDTO dto) throws MiException {
        Cliente cliente = new Cliente();
        copiarDatosPersonales(cliente, dto);
        ProveedorAutenticacion proveedor = dto.proveedorAutenticacion() == null
                ? ProveedorAutenticacion.LOCAL : dto.proveedorAutenticacion();
        cliente.setTipoDocumento(dto.tipoDocumento());
        cliente.setNumeroDocumento(ValidacionesPersona.normalizarDocumento(dto.numeroDocumento()));
        cliente.setUsuario(dto.claveTemporal()
                ? servicioUsuario.crearCuentaClienteConClaveTemporal(dto.correo())
                : servicioUsuario.crearUsuario(dto.correo(), RolUsuario.CLIENTE, dto.clave(), proveedor));
        return cliente;
    }

    @Override
    protected void actualizarEntidad(Cliente cliente, ClienteRequestDTO dto) throws MiException {
        if (!estaVacio(dto.correo())
                && !ServicioUsuario.normalizarCorreo(dto.correo()).equals(cliente.getUsuario().getNombreUsuario())) {
            throw new MiException("El correo no se puede modificar: es el usuario de ingreso");
        }
        copiarDatosPersonales(cliente, dto);
        ValidacionesPersona.aplicarDocumentoEnModificacion(cliente, dto.tipoDocumento(), dto.numeroDocumento());
    }

    @Override
    protected void validar(BaseUseCaseService casoDeUso, Cliente cliente) throws MiException {
        if (casoDeUso == BaseUseCaseService.BAJA) {
            return;
        }
        if (cliente.getUsuario() == null || cliente.getUsuario().getRol() != RolUsuario.CLIENTE) {
            throw new MiException("El cliente debe tener una cuenta con rol CLIENTE");
        }
        ValidacionesPersona.validarNombreYApellido(cliente);
        ValidacionesPersona.validarDocumento(cliente.getTipoDocumento(), cliente.getNumeroDocumento(), false);
        ValidacionesPersona.validarMayorDeEdad(cliente.getFechaNacimiento(), false);
        if (cliente.getTipoDocumento() != null) {
            Optional<Cliente> mismoDocumento = repositorioCliente.findFirstByTipoDocumentoAndNumeroDocumentoAndEliminadoFalse(
                    cliente.getTipoDocumento(), cliente.getNumeroDocumento());
            if (mismoDocumento.isPresent() && !Objects.equals(mismoDocumento.get().getId(), cliente.getId())) {
                throw new MiException("Ya existe un cliente con el documento " + cliente.getNumeroDocumento());
            }
        }
    }

    @Override
    protected void postBaja(Cliente cliente) throws MiException {
        if (cliente.getUsuario() != null && !cliente.getUsuario().isEliminado()) {
            servicioUsuario.baja(cliente.getUsuario().getId());
        }
    }

    @Override
    public ClienteResponseDTO convertirAResponse(Cliente cliente) {
        Usuario usuario = cliente.getUsuario();
        return new ClienteResponseDTO(cliente.getId(), cliente.getNombre(), cliente.getApellido(),
                cliente.getFechaNacimiento(), cliente.getTipoDocumento(), cliente.getNumeroDocumento(),
                usuario.getNombreUsuario(), usuario.getProveedorAutenticacion(), cliente.isEliminado());
    }

    @Override
    protected String nombreEntidad() {
        return "el cliente";
    }

    // ------------------------------------------------------------------
    // Operaciones propias (diagrama de clases)
    // ------------------------------------------------------------------

    /** registrarCliente(nombre, apellido, email, clave) del diagrama: registro desde el sitio institucional. */
    @Transactional(rollbackFor = Exception.class)
    public Cliente registrarCliente(RegistroClienteDTO registro) throws MiException {
        if (registro == null) {
            throw new MiException("Debe indicar los datos del registro");
        }
        if (registro.clave() == null || !registro.clave().equals(registro.confirmacionClave())) {
            throw new MiException("La contraseña y su confirmación no coinciden");
        }
        exigirDocumentoYNacimiento(registro.tipoDocumento(), registro.numeroDocumento(), registro.fechaNacimiento());
        return altaEntidad(new ClienteRequestDTO(registro.nombre(), registro.apellido(), registro.fechaNacimiento(),
                registro.tipoDocumento(), registro.numeroDocumento(), registro.correo(), registro.clave(),
                ProveedorAutenticacion.LOCAL));
    }

    /**
     * registrarClienteRedSocial(token, proveedor) del diagrama. La identidad ya fue verificada por Spring Security
     * (OAuth2/OIDC); acá se crea el cliente sin documento ni clave: puede completar sus datos desde "Mi cuenta".
     */
    @Transactional(rollbackFor = Exception.class)
    public Cliente registrarClienteRedSocial(String correo, String nombre, String apellido,
                                             ProveedorAutenticacion proveedor) throws MiException {
        if (proveedor == null || proveedor == ProveedorAutenticacion.LOCAL) {
            throw new MiException("El registro con redes sociales requiere Google o Facebook");
        }
        String nombrePorDefecto = correo == null || !correo.contains("@") ? "Cliente" : correo.substring(0, correo.indexOf('@'));
        return altaEntidad(new ClienteRequestDTO(
                ValidacionesPersona.nombreValido(nombre, ValidacionesPersona.nombreValido(nombrePorDefecto, "Cliente")),
                ValidacionesPersona.nombreValido(apellido, "Sin apellido"),
                null, null, null, correo, null, proveedor));
    }

    /**
     * Alta de un cliente desde el celular del preventista. La cuenta queda LOCAL, con rol CLIENTE, la clave temporal
     * "dulcemayor" (guardada con BCrypt) y debeCambiarClave = true: hasta cambiarla, la API solo le permite el
     * cambio de clave (mismo mecanismo que los empleados, HU-06). Documento y fecha de nacimiento son obligatorios.
     * La dirección particular es opcional y se crea por ServicioDireccion, en la misma transacción.
     */
    @Transactional(rollbackFor = Exception.class)
    public ClienteResponseDTO registrarClienteEnCampo(ClienteCampoDTO alta) throws MiException {
        if (alta == null) {
            throw new MiException("Debe indicar los datos del cliente");
        }
        exigirDocumentoYNacimiento(alta.tipoDocumento(), alta.numeroDocumento(), alta.fechaNacimiento());
        Cliente cliente = altaEntidad(new ClienteRequestDTO(alta.nombre(), alta.apellido(), alta.fechaNacimiento(),
                alta.tipoDocumento(), alta.numeroDocumento(), alta.correo(), null, ProveedorAutenticacion.LOCAL, true));
        if (alta.direccion() != null) {
            cliente.setDireccion(servicioDireccion.crearDireccion(alta.direccion()));
            cliente = repositorioCliente.save(cliente);
        }
        return convertirAResponse(cliente);
    }

    /** Busca un cliente activo por el correo de su cuenta (para asociarle un punto de venta). */
    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarClientePorCorreo(String correo) throws MiException {
        if (estaVacio(correo)) {
            throw new MiException("Debe indicar el correo a buscar");
        }
        return convertirAResponse(clienteDeLaCuenta(ServicioUsuario.normalizarCorreo(correo)));
    }

    /** Cliente dueño de la cuenta autenticada. Lo usan los pedidos para verificar la propiedad. */
    @Transactional(readOnly = true)
    public Cliente obtenerClientePorCorreo(String correo) throws MiException {
        return clienteDeLaCuenta(correo);
    }

    /** buscarClientePorUsuario(idUsuario) del diagrama. */
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarClientePorUsuario(String idUsuario) {
        return repositorioCliente.findFirstByUsuario_Id(idUsuario);
    }

    /** Datos del cliente autenticado (se identifica por el correo del token, nunca por un id enviado). */
    @Transactional(readOnly = true)
    public ClienteResponseDTO consultarMisDatos(String correo) throws MiException {
        return convertirAResponse(clienteDeLaCuenta(correo));
    }

    /** El cliente autenticado modifica sus datos personales. Usa el algoritmo de modificación. */
    @Transactional(rollbackFor = Exception.class)
    public ClienteResponseDTO actualizarDatosPersonales(String correo, DatosPersonalesDTO datos) throws MiException {
        if (datos == null) {
            throw new MiException("Debe indicar los datos a modificar");
        }
        Cliente cliente = clienteDeLaCuenta(correo);
        return modificar(cliente.getId(), new ClienteRequestDTO(datos.nombre(), datos.apellido(),
                datos.fechaNacimiento(), datos.tipoDocumento(), datos.numeroDocumento(), null, null, null));
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private Cliente clienteDeLaCuenta(String correo) throws MiException {
        Usuario usuario = servicioUsuario.buscarActivoPorNombreUsuario(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la cuenta indicada"));
        return buscarClientePorUsuario(usuario.getId())
                .filter(cliente -> !cliente.isEliminado())
                .orElseThrow(() -> new RecursoNoEncontradoException("La cuenta no pertenece a un cliente"));
    }

    /** Nombre, apellido y fecha de nacimiento. El documento se trata aparte (no se modifica una vez cargado). */
    private static void copiarDatosPersonales(Cliente cliente, ClienteRequestDTO dto) {
        cliente.setNombre(normalizarTexto(dto.nombre()));
        cliente.setApellido(normalizarTexto(dto.apellido()));
        cliente.setFechaNacimiento(dto.fechaNacimiento());
    }

    /**
     * Registro en el sitio y alta desde la app del preventista: documento y fecha de nacimiento obligatorios.
     * En la base siguen siendo opcionales porque los clientes de Google o Facebook se crean sin ellos.
     */
    private static void exigirDocumentoYNacimiento(TipoDocumento tipo, String numero, LocalDate nacimiento)
            throws MiException {
        ValidacionesPersona.validarDocumento(tipo, ValidacionesPersona.normalizarDocumento(numero), true);
        ValidacionesPersona.validarMayorDeEdad(nacimiento, true);
    }
}
