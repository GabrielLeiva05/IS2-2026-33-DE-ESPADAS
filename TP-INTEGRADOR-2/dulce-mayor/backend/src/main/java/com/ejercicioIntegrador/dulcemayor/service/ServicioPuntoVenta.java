package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.DireccionRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PuntoVentaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PuntoVentaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Cliente;
import com.ejercicioIntegrador.dulcemayor.model.Direccion;
import com.ejercicioIntegrador.dulcemayor.model.PuntoVenta;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPuntoVenta;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseUseCaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Puntos de venta (HU-17): el comercio de un cliente, con su dirección COMERCIAL y sus coordenadas.
 * La dirección se crea a través de ServicioDireccion y nunca se reutiliza la dirección particular del cliente.
 * Las coordenadas son obligatorias: sin ellas el punto no puede ubicarse en el mapa ni en una ruta,
 * y no se inventan coordenadas por defecto.
 */
@Service
public class ServicioPuntoVenta extends BaseService<PuntoVenta, PuntoVentaRequestDTO, PuntoVentaResponseDTO> {

    private static final int[] PESOS_CUIT = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    private final RepositorioPuntoVenta repositorioPuntoVenta;
    private final ServicioCliente servicioCliente;
    private final ServicioDireccion servicioDireccion;

    public ServicioPuntoVenta(RepositorioPuntoVenta repositorioPuntoVenta, ServicioCliente servicioCliente,
                              ServicioDireccion servicioDireccion) {
        super(repositorioPuntoVenta);
        this.repositorioPuntoVenta = repositorioPuntoVenta;
        this.servicioCliente = servicioCliente;
        this.servicioDireccion = servicioDireccion;
    }

    // ------------------------------------------------------------------
    // Pasos del Template Method
    // ------------------------------------------------------------------

    @Override
    protected PuntoVenta crearEntidad(PuntoVentaRequestDTO dto) throws MiException {
        PuntoVenta puntoVenta = new PuntoVenta();
        copiarDatos(puntoVenta, dto);
        puntoVenta.setCliente(obtenerReferencia(servicioCliente, dto.idCliente(), "el cliente"));
        verificarUbicacion(dto.direccion());
        puntoVenta.setDireccion(servicioDireccion.crearDireccion(dto.direccion()));
        return puntoVenta;
    }

    @Override
    protected void actualizarEntidad(PuntoVenta puntoVenta, PuntoVentaRequestDTO dto) throws MiException {
        if (!estaVacio(dto.idCliente()) && !dto.idCliente().equals(puntoVenta.getCliente().getId())) {
            throw new MiException("El cliente de un punto de venta no se puede cambiar");
        }
        copiarDatos(puntoVenta, dto);
        if (dto.direccion() != null) {
            verificarUbicacion(dto.direccion());
            servicioDireccion.modificar(puntoVenta.getDireccion().getId(), dto.direccion());
        }
    }

    @Override
    protected void validar(BaseUseCaseService casoDeUso, PuntoVenta puntoVenta) throws MiException {
        if (casoDeUso == BaseUseCaseService.BAJA) {
            return;
        }
        if (estaVacio(puntoVenta.getNombreFantasia()) || puntoVenta.getNombreFantasia().length() > 120) {
            throw new MiException("Debe indicar el nombre de fantasía del comercio (hasta 120 caracteres)");
        }
        if (estaVacio(puntoVenta.getRazonSocial()) || puntoVenta.getRazonSocial().length() > 150) {
            throw new MiException("Debe indicar la razón social (hasta 150 caracteres)");
        }
        if (puntoVenta.getCondicionIva() == null) {
            throw new MiException("Debe indicar la condición frente al IVA");
        }
        if (!cuitValido(puntoVenta.getCuit())) {
            throw new MiException("El CUIT debe tener 11 dígitos y un dígito verificador válido");
        }
        Optional<PuntoVenta> mismoCuit = repositorioPuntoVenta.findFirstByCuitAndEliminadoFalse(puntoVenta.getCuit());
        if (mismoCuit.isPresent() && !Objects.equals(mismoCuit.get().getId(), puntoVenta.getId())) {
            throw new MiException("Ya existe un punto de venta con el CUIT " + puntoVenta.getCuit());
        }
        Cliente cliente = puntoVenta.getCliente();
        if (cliente == null || cliente.isEliminado()) {
            throw new MiException("El punto de venta debe pertenecer a un cliente activo");
        }
        Direccion direccion = puntoVenta.getDireccion();
        if (direccion == null || direccion.getLatitud() == null || direccion.getLongitud() == null) {
            throw new MiException("El punto de venta debe tener su ubicación (latitud y longitud)");
        }
    }

    @Override
    public PuntoVentaResponseDTO convertirAResponse(PuntoVenta puntoVenta) {
        Cliente cliente = puntoVenta.getCliente();
        String correo = cliente.getUsuario() == null ? null : cliente.getUsuario().getNombreUsuario();
        return new PuntoVentaResponseDTO(puntoVenta.getId(), puntoVenta.getNombreFantasia(),
                puntoVenta.getRazonSocial(), puntoVenta.getCuit(), puntoVenta.getCondicionIva(), cliente.getId(),
                cliente.getNombre() + " " + cliente.getApellido(), correo,
                servicioDireccion.convertirAResponse(puntoVenta.getDireccion()), puntoVenta.isEliminado());
    }

    @Override
    protected String nombreEntidad() {
        return "el punto de venta";
    }

    // ------------------------------------------------------------------
    // Operaciones propias (diagrama de clases)
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PuntoVentaResponseDTO buscarPuntoVentaPorCuit(String cuit) throws MiException {
        String normalizado = soloDigitos(cuit);
        return repositorioPuntoVenta.findFirstByCuitAndEliminadoFalse(normalizado)
                .map(this::convertirAResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un punto de venta con el CUIT " + cuit));
    }

    @Transactional(readOnly = true)
    public List<PuntoVentaResponseDTO> listarPuntoVentaPorCliente(String idCliente) throws MiException {
        servicioCliente.obtenerEntidad(idCliente);
        return convertirAResponse(repositorioPuntoVenta.findByCliente_IdAndEliminadoFalseOrderByNombreFantasiaAsc(idCliente));
    }

    /** "Calle 123, Localidad" para listas y marcadores. */
    public static String textoDireccion(PuntoVenta puntoVenta) {
        Direccion d = puntoVenta.getDireccion();
        if (d == null) {
            return "";
        }
        String localidad = d.getLocalidad() == null ? "" : ", " + d.getLocalidad().getNombre();
        return d.getCalle() + " " + d.getNumeracion() + localidad;
    }

    /** Verifica el dígito verificador del CUIT (módulo 11). */
    static boolean cuitValido(String cuit) {
        if (cuit == null || !cuit.matches("\\d{11}")) {
            return false;
        }
        int suma = 0;
        for (int i = 0; i < 10; i++) {
            suma += (cuit.charAt(i) - '0') * PESOS_CUIT[i];
        }
        int verificador = 11 - (suma % 11);
        if (verificador == 11) {
            verificador = 0;
        }
        return verificador != 10 && verificador == cuit.charAt(10) - '0';
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private static void copiarDatos(PuntoVenta puntoVenta, PuntoVentaRequestDTO dto) {
        puntoVenta.setNombreFantasia(normalizarTexto(dto.nombreFantasia()));
        puntoVenta.setRazonSocial(normalizarTexto(dto.razonSocial()));
        puntoVenta.setCuit(soloDigitos(dto.cuit()));
        puntoVenta.setCondicionIva(dto.condicionIva());
    }

    /** No se guardan comercios sin coordenadas ni con coordenadas incompletas. */
    private static void verificarUbicacion(DireccionRequestDTO direccion) throws MiException {
        if (direccion == null) {
            throw new MiException("Debe indicar la dirección del comercio");
        }
        if (direccion.latitud() == null || direccion.longitud() == null) {
            throw new MiException("Debe ubicar el comercio en el mapa: buscá la dirección o tocá el mapa");
        }
    }

    private static String soloDigitos(String texto) {
        return texto == null ? null : texto.replaceAll("\\D", "");
    }
}
