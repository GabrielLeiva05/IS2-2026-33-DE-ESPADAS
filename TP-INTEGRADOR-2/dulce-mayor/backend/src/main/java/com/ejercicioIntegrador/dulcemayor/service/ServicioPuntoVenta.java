package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.PuntoVentaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PuntoVentaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Cliente;
import com.ejercicioIntegrador.dulcemayor.model.Direccion;
import com.ejercicioIntegrador.dulcemayor.model.PuntoVenta;
import com.ejercicioIntegrador.dulcemayor.model.Preventista;
import com.ejercicioIntegrador.dulcemayor.model.Usuario;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioCliente;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPuntoVenta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class ServicioPuntoVenta {

    private final RepositorioPuntoVenta repositorioPuntoVenta;
    private final RepositorioCliente repositorioCliente;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioUsuario servicioUsuario;
    private final ServicioDireccion servicioDireccion;

    public ServicioPuntoVenta(RepositorioPuntoVenta repositorioPuntoVenta, RepositorioCliente repositorioCliente,
                              ServicioEmpleado servicioEmpleado, ServicioUsuario servicioUsuario,
                              ServicioDireccion servicioDireccion) {
        this.repositorioPuntoVenta = repositorioPuntoVenta;
        this.repositorioCliente = repositorioCliente;
        this.servicioEmpleado = servicioEmpleado;
        this.servicioUsuario = servicioUsuario;
        this.servicioDireccion = servicioDireccion;
    }

    @Transactional(rollbackFor = Exception.class)
    public PuntoVentaResponseDTO registrar(PuntoVentaRequestDTO dto, String correoPreventista) throws MiException {
        validarSolicitud(dto);
        Usuario cuentaPreventista = servicioUsuario.buscarActivoPorNombreUsuario(correoPreventista)
                .orElseThrow(() -> new MiException("No se encontró la cuenta del preventista autenticado"));
        if (cuentaPreventista.getRol() != RolUsuario.PREVENTISTA && cuentaPreventista.getRol() != RolUsuario.JEFE) {
            throw new MiException("La cuenta autenticada no tiene perfil de preventista");
        }
        Preventista preventista = servicioEmpleado.buscarPorUsuario(cuentaPreventista.getId())
                .filter(Preventista.class::isInstance)
                .map(Preventista.class::cast)
                .orElseThrow(() -> new MiException("No se encontró el preventista asociado a la cuenta"));

        TipoDocumento tipo = dto.tipoDocumentoCliente();
        String numero = ValidacionesPersona.normalizarDocumento(dto.numeroDocumentoCliente());
        if (tipo != null && repositorioCliente.findFirstByTipoDocumentoAndNumeroDocumentoAndEliminadoFalse(tipo, numero).isPresent()) {
            throw new MiException("Ya existe un responsable con ese documento; solicite revisión administrativa");
        }
        Cliente cliente = new Cliente();
        cliente.setNombre(dto.nombreCliente().trim());
        cliente.setApellido(dto.apellidoCliente().trim());
        cliente.setFechaNacimiento(dto.fechaNacimientoCliente());
        cliente.setTipoDocumento(tipo);
        cliente.setNumeroDocumento(numero);
        ValidacionesPersona.validarNombreYApellido(cliente);
        ValidacionesPersona.validarDocumento(tipo, numero, false);
        Cliente clienteGuardado = repositorioCliente.save(cliente);

        Direccion direccion = servicioDireccion.crearDireccion(dto.direccion());
        PuntoVenta puntoVenta = new PuntoVenta();
        puntoVenta.setNombreComercial(dto.nombreComercial().trim());
        puntoVenta.setTelefono(normalizarOpcional(dto.telefono()));
        puntoVenta.setEmailContacto(normalizarEmailOpcional(dto.emailContacto()));
        puntoVenta.setCliente(clienteGuardado);
        puntoVenta.setDireccion(direccion);
        puntoVenta.setPreventista(preventista);
        return convertir(repositorioPuntoVenta.save(puntoVenta));
    }

    @Transactional(readOnly = true)
    public List<PuntoVentaResponseDTO> listarTodos() {
        return repositorioPuntoVenta.findByEliminadoFalse().stream()
                .sorted(Comparator.comparing(PuntoVenta::getNombreComercial, String.CASE_INSENSITIVE_ORDER))
                .map(this::convertir).toList();
    }

    @Transactional(readOnly = true)
    public List<PuntoVentaResponseDTO> listarCartera(String correoPreventista) throws MiException {
        Preventista preventista = preventistaDeCuenta(correoPreventista);
        // El jefe no tiene cartera propia: consulta la de todos los preventistas.
        List<PuntoVenta> puntos = preventista == null
                ? repositorioPuntoVenta.findByEliminadoFalse().stream()
                        .sorted(Comparator.comparing(PuntoVenta::getNombreComercial, String.CASE_INSENSITIVE_ORDER))
                        .toList()
                : repositorioPuntoVenta.findByPreventista_IdAndEliminadoFalseOrderByNombreComercialAsc(preventista.getId());
        return puntos.stream().map(this::convertir).toList();
    }

    @Transactional(readOnly = true)
    public List<PuntoVentaResponseDTO> listarPuntosCliente(String idCliente, String correoPreventista) throws MiException {
        Preventista preventista = preventistaDeCuenta(correoPreventista);
        return repositorioPuntoVenta.findByCliente_IdAndEliminadoFalseOrderByNombreComercialAsc(idCliente).stream()
                .filter(punto -> preventista == null
                        || Objects.equals(punto.getPreventista().getId(), preventista.getId()))
                .map(this::convertir).toList();
    }

    @Transactional(readOnly = true)
    public PuntoVenta obtenerPuntoPropio(String idPuntoVenta, String correoPreventista) throws MiException {
        Preventista preventista = preventistaDeCuenta(correoPreventista);
        return repositorioPuntoVenta.findByIdAndEliminadoFalse(idPuntoVenta)
                .filter(punto -> preventista == null || Objects.equals(punto.getPreventista().getId(), preventista.getId()))
                .orElseThrow(() -> new MiException("El punto de venta no pertenece a la cartera del preventista"));
    }

    @Transactional(readOnly = true)
    public PuntoVentaResponseDTO convertir(PuntoVenta punto) {
        Cliente cliente = punto.getCliente();
        return new PuntoVentaResponseDTO(punto.getId(), cliente.getId(), cliente.getNombre(), cliente.getApellido(),
                punto.getPreventista().getId(), punto.getNombreComercial(), punto.getTelefono(), punto.getEmailContacto(),
                servicioDireccion.convertirAResponse(punto.getDireccion()), punto.isEliminado());
    }

    private Preventista preventistaDeCuenta(String correo) throws MiException {
        Usuario usuario = servicioUsuario.buscarActivoPorNombreUsuario(correo)
                .orElseThrow(() -> new MiException("No se encontró la cuenta autenticada"));
        if (usuario.getRol() == RolUsuario.JEFE) {
            return null;
        }
        if (usuario.getRol() != RolUsuario.PREVENTISTA) {
            throw new MiException("La cuenta autenticada no tiene perfil de preventista");
        }
        return servicioEmpleado.buscarPorUsuario(usuario.getId()).filter(Preventista.class::isInstance)
                .map(Preventista.class::cast)
                .orElseThrow(() -> new MiException("No se encontró el preventista asociado a la cuenta"));
    }

    private static void validarSolicitud(PuntoVentaRequestDTO dto) throws MiException {
        if (dto == null || dto.direccion() == null) {
            throw new MiException("Debe indicar los datos del responsable, el comercio y su dirección");
        }
        if (vacio(dto.nombreCliente()) || vacio(dto.apellidoCliente()) || vacio(dto.nombreComercial())) {
            throw new MiException("Debe indicar nombre y apellido del responsable y el nombre comercial");
        }
        if (vacio(dto.correoContactoCliente()) || !dto.correoContactoCliente().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new MiException("Debe indicar un correo de contacto válido para el responsable");
        }
        if (vacio(dto.numeroDocumentoCliente())) {
            throw new MiException("Debe indicar el documento del responsable");
        }
        if (dto.tipoDocumentoCliente() == null) {
            throw new MiException("Debe indicar el tipo de documento del responsable");
        }
        if (dto.direccion().latitud() == null || dto.direccion().longitud() == null
                || !Double.isFinite(dto.direccion().latitud()) || !Double.isFinite(dto.direccion().longitud())
                || dto.direccion().latitud() < -90 || dto.direccion().latitud() > 90
                || dto.direccion().longitud() < -180 || dto.direccion().longitud() > 180) {
            throw new MiException("Debe indicar coordenadas válidas para el punto de venta");
        }
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static String normalizarOpcional(String valor) {
        return vacio(valor) ? null : valor.trim();
    }

    private static String normalizarEmailOpcional(String valor) throws MiException {
        if (vacio(valor)) {
            return null;
        }
        String email = valor.trim();
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new MiException("Debe indicar un correo de contacto válido para el comercio");
        }
        return email;
    }
}
