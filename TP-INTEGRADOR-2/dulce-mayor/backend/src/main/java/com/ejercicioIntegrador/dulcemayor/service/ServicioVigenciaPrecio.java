package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.VigenciaPrecioRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.VigenciaPrecioResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.VigenciaPrecio;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioVigenciaPrecio;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseUseCaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Precios mayoristas con vigencia. Reutiliza la regla del Integrador 1 (al crear un precio nuevo se cierra
 * la vigencia abierta) y la ajusta a la nota del diagrama: la vigencia anterior se cierra el día previo al
 * inicio de la nueva, de modo que nunca hay dos precios para la misma fecha.
 */
@Service
public class ServicioVigenciaPrecio extends BaseService<VigenciaPrecio, VigenciaPrecioRequestDTO, VigenciaPrecioResponseDTO> {

    private final RepositorioVigenciaPrecio repositorioVigenciaPrecio;
    private final ServicioProducto servicioProducto;

    public ServicioVigenciaPrecio(RepositorioVigenciaPrecio repositorioVigenciaPrecio, ServicioProducto servicioProducto) {
        super(repositorioVigenciaPrecio);
        this.repositorioVigenciaPrecio = repositorioVigenciaPrecio;
        this.servicioProducto = servicioProducto;
    }

    // ------------------------------------------------------------------
    // Pasos del Template Method
    // ------------------------------------------------------------------

    @Override
    protected VigenciaPrecio crearEntidad(VigenciaPrecioRequestDTO dto) throws MiException {
        VigenciaPrecio vigencia = new VigenciaPrecio();
        vigencia.setProducto(obtenerReferencia(servicioProducto, dto.idProducto(), "el producto"));
        copiarDatos(vigencia, dto);
        return vigencia;
    }

    @Override
    protected void actualizarEntidad(VigenciaPrecio vigencia, VigenciaPrecioRequestDTO dto) throws MiException {
        if (dto.idProducto() != null && !dto.idProducto().equals(vigencia.getProducto().getId())) {
            throw new MiException("No se puede cambiar el producto de una vigencia de precio");
        }
        copiarDatos(vigencia, dto);
    }

    @Override
    protected void validar(BaseUseCaseService casoDeUso, VigenciaPrecio vigencia) throws MiException {
        if (casoDeUso == BaseUseCaseService.BAJA) {
            return;
        }
        if (vigencia.getFechaDesde() == null) {
            throw new MiException("La fecha desde es obligatoria");
        }
        if (vigencia.getFechaHasta() != null && vigencia.getFechaHasta().isBefore(vigencia.getFechaDesde())) {
            throw new MiException("La fecha hasta no puede ser anterior a la fecha desde");
        }
        if (vigencia.getPrecio() <= 0) {
            throw new MiException("El precio debe ser mayor a cero");
        }

        List<VigenciaPrecio> otras = otrasVigenciasActivas(vigencia);
        boolean esAlta = casoDeUso == BaseUseCaseService.ALTA;

        if (esAlta) {
            Optional<VigenciaPrecio> abierta = buscarAbierta(otras);
            if (abierta.isPresent() && !vigencia.getFechaDesde().isAfter(abierta.get().getFechaDesde())) {
                throw new MiException("La nueva vigencia debe comenzar después del " + abierta.get().getFechaDesde()
                        + ", fecha de inicio del precio actual");
            }
        }
        for (VigenciaPrecio otra : otras) {
            // En el alta, la vigencia abierta se cerrará el día anterior al inicio de la nueva (preAlta)
            LocalDate hastaOtra = esAlta && otra.getFechaHasta() == null
                    ? vigencia.getFechaDesde().minusDays(1)
                    : otra.getFechaHasta();
            if (seSuperponen(vigencia.getFechaDesde(), vigencia.getFechaHasta(), otra.getFechaDesde(), hastaOtra)) {
                throw new MiException("El período se superpone con la vigencia del " + otra.getFechaDesde()
                        + (hastaOtra == null ? " sin fecha de fin" : " al " + hastaOtra));
            }
        }
    }

    /** Cierra la vigencia abierta del producto el día anterior al inicio de la nueva. */
    @Override
    protected void preAlta(VigenciaPrecio nueva) {
        buscarAbierta(otrasVigenciasActivas(nueva)).ifPresent(abierta -> {
            abierta.setFechaHasta(nueva.getFechaDesde().minusDays(1));
            repositorioVigenciaPrecio.save(abierta);
        });
    }

    @Override
    public VigenciaPrecioResponseDTO convertirAResponse(VigenciaPrecio vigencia) {
        boolean vigente = !vigencia.isEliminado() && cubreFecha(vigencia, LocalDate.now());
        return new VigenciaPrecioResponseDTO(vigencia.getId(), vigencia.getProducto().getId(),
                vigencia.getProducto().getNombre(), vigencia.getFechaDesde(), vigencia.getFechaHasta(),
                vigencia.getPrecio(), vigente, vigencia.isEliminado());
    }

    @Override
    protected String nombreEntidad() {
        return "la vigencia de precio";
    }

    // ------------------------------------------------------------------
    // Operaciones propias (diagrama de clases)
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public VigenciaPrecioResponseDTO buscarVigenciaPrecioVigente(String idProducto) throws MiException {
        servicioProducto.obtenerEntidad(idProducto);
        return obtenerVigenciaVigente(idProducto)
                .map(this::convertirAResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto no tiene un precio vigente"));
    }

    @Transactional(readOnly = true)
    public List<VigenciaPrecioResponseDTO> listarVigenciaPrecio(String idProducto) throws MiException {
        servicioProducto.obtenerEntidad(idProducto);
        return convertirAResponse(repositorioVigenciaPrecio.findByProducto_IdOrderByFechaDesdeDesc(idProducto));
    }

    @Transactional(readOnly = true)
    public List<VigenciaPrecioResponseDTO> listarVigenciaPrecioActivo(String idProducto) throws MiException {
        servicioProducto.obtenerEntidad(idProducto);
        return convertirAResponse(repositorioVigenciaPrecio.findByProducto_IdAndEliminadoFalseOrderByFechaDesdeDesc(idProducto));
    }

    /**
     * Precio vigente hoy, para uso de otros servicios (catálogo y, más adelante, pedidos).
     * Devuelve vacío si el producto todavía no tiene precio para la fecha actual.
     */
    @Transactional(readOnly = true)
    public Optional<VigenciaPrecio> obtenerVigenciaVigente(String idProducto) {
        LocalDate hoy = LocalDate.now();
        return repositorioVigenciaPrecio.findByProducto_IdAndEliminadoFalseOrderByFechaDesdeDesc(idProducto).stream()
                .filter(vigencia -> cubreFecha(vigencia, hoy))
                .max(Comparator.comparing(VigenciaPrecio::getFechaDesde));
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private void copiarDatos(VigenciaPrecio vigencia, VigenciaPrecioRequestDTO dto) {
        vigencia.setFechaDesde(dto.fechaDesde());
        vigencia.setFechaHasta(dto.fechaHasta());
        vigencia.setPrecio(dto.precio() == null ? 0 : dto.precio());
    }

    private List<VigenciaPrecio> otrasVigenciasActivas(VigenciaPrecio vigencia) {
        return repositorioVigenciaPrecio
                .findByProducto_IdAndEliminadoFalseOrderByFechaDesdeDesc(vigencia.getProducto().getId()).stream()
                .filter(otra -> !Objects.equals(otra.getId(), vigencia.getId()))
                .toList();
    }

    private static Optional<VigenciaPrecio> buscarAbierta(List<VigenciaPrecio> vigencias) {
        return vigencias.stream().filter(vigencia -> vigencia.getFechaHasta() == null).findFirst();
    }

    private static boolean cubreFecha(VigenciaPrecio vigencia, LocalDate fecha) {
        return !vigencia.getFechaDesde().isAfter(fecha)
                && (vigencia.getFechaHasta() == null || !vigencia.getFechaHasta().isBefore(fecha));
    }

    /** Dos períodos se superponen si ninguno termina antes de que empiece el otro (null = sin fin). */
    private static boolean seSuperponen(LocalDate desde1, LocalDate hasta1, LocalDate desde2, LocalDate hasta2) {
        boolean primeroTerminaAntes = hasta1 != null && hasta1.isBefore(desde2);
        boolean segundoTerminaAntes = hasta2 != null && hasta2.isBefore(desde1);
        return !primeroTerminaAntes && !segundoTerminaAntes;
    }
}
