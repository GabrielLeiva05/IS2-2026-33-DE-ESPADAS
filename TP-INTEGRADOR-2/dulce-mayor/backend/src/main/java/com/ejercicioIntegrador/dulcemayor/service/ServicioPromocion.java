package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.DescuentoPromocionDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PromocionRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PromocionResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoPromocion;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Promocion;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPedido;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPromocion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Promociones: un solo código por pedido y sin acumulación. El descuento se calcula siempre en el
 * servidor y se congela como snapshot en el pedido (nunca se acepta un monto calculado por el cliente).
 */
@Service
public class ServicioPromocion {

    private static final int ESCALA = 2;
    private static final List<EstadoPedido> ESTADOS_CONSUMIDOS =
            List.of(EstadoPedido.CONFIRMADO, EstadoPedido.COMPLETADO);

    private final RepositorioPromocion repositorioPromocion;
    private final RepositorioPedido repositorioPedido;

    public ServicioPromocion(RepositorioPromocion repositorioPromocion, RepositorioPedido repositorioPedido) {
        this.repositorioPromocion = repositorioPromocion;
        this.repositorioPedido = repositorioPedido;
    }

    // ------------------------------------------------------------------ administración

    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> listar() {
        return repositorioPromocion.findByEliminadoFalse().stream()
                .sorted(Comparator.comparing(Promocion::getFechaDesde).reversed())
                .map(this::convertir).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public PromocionResponseDTO alta(PromocionRequestDTO dto) throws MiException {
        validar(dto);
        String codigo = normalizar(dto.codigo());
        if (repositorioPromocion.existsByCodigoIgnoreCaseAndEliminadoFalse(codigo)) {
            throw new MiException("Ya existe una promoción con el código " + codigo);
        }
        Promocion promocion = new Promocion();
        copiar(promocion, dto, codigo);
        return convertir(repositorioPromocion.save(promocion));
    }

    @Transactional(rollbackFor = Exception.class)
    public PromocionResponseDTO modificar(String id, PromocionRequestDTO dto) throws MiException {
        validar(dto);
        Promocion promocion = repositorioPromocion.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new MiException("No existe la promoción indicada"));
        String codigo = normalizar(dto.codigo());
        var existente = repositorioPromocion.findFirstByCodigoIgnoreCaseAndEliminadoFalse(codigo);
        if (existente.isPresent() && !existente.get().getId().equals(promocion.getId())) {
            throw new MiException("Ya existe una promoción con el código " + codigo);
        }
        copiar(promocion, dto, codigo);
        return convertir(repositorioPromocion.save(promocion));
    }

    @Transactional(rollbackFor = Exception.class)
    public void baja(String id) throws MiException {
        Promocion promocion = repositorioPromocion.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new MiException("No existe la promoción indicada"));
        promocion.setEliminado(true);
        promocion.setActiva(false);
        repositorioPromocion.save(promocion);
    }

    // ------------------------------------------------------------------ aplicación

    /** Vista previa para administración: no exige un pedido en curso. */
    @Transactional(readOnly = true)
    public DescuentoPromocionDTO previsualizar(String codigo, BigDecimal subtotal) throws MiException {
        return calcular(codigo, subtotal, null);
    }

    /**
     * Calcula el descuento que corresponde a un pedido. {@code idPedido} es el pedido que va a
     * quedarse con el código (se excluye del control de uso único).
     */
    @Transactional(readOnly = true)
    public DescuentoPromocionDTO calcular(String codigo, BigDecimal subtotal, String idPedido) throws MiException {
        if (codigo == null || codigo.isBlank()) {
            throw new MiException("Indicá el código promocional");
        }
        if (subtotal == null || subtotal.signum() <= 0) {
            throw new MiException("El pedido no tiene importe para descontar");
        }
        Promocion promocion = repositorioPromocion
                .findFirstByCodigoIgnoreCaseAndEliminadoFalse(codigo.trim())
                .orElseThrow(() -> new MiException("El código promocional no existe"));
        LocalDate hoy = LocalDate.now();
        if (!promocion.vigente(hoy)) {
            throw new MiException("El código " + promocion.getCodigo() + " no está vigente");
        }
        if (promocion.getMinimoCompra() != null && subtotal.compareTo(promocion.getMinimoCompra()) < 0) {
            throw new MiException("El código requiere una compra mínima de $ " + promocion.getMinimoCompra());
        }
        if (promocion.isUsoUnico()) {
            long usos = idPedido == null
                    ? repositorioPedido.countByPromocionCodigoIgnoreCaseAndEstadoInAndEliminadoFalse(
                            promocion.getCodigo(), ESTADOS_CONSUMIDOS)
                    : repositorioPedido.countByPromocionCodigoIgnoreCaseAndEstadoInAndEliminadoFalseAndIdNot(
                            promocion.getCodigo(), ESTADOS_CONSUMIDOS, idPedido);
            if (usos > 0) {
                throw new MiException("El código " + promocion.getCodigo() + " ya fue utilizado");
            }
        }

        BigDecimal descuento = promocion.getTipo() == TipoPromocion.PORCENTAJE
                ? subtotal.multiply(promocion.getValor()).divide(BigDecimal.valueOf(100), ESCALA, RoundingMode.HALF_UP)
                : promocion.getValor().setScale(ESCALA, RoundingMode.HALF_UP);
        if (descuento.compareTo(subtotal) > 0) {
            descuento = subtotal;
        }
        if (descuento.signum() <= 0) {
            throw new MiException("El código " + promocion.getCodigo() + " no genera descuento en este pedido");
        }
        return new DescuentoPromocionDTO(promocion.getCodigo(), subtotal.setScale(ESCALA, RoundingMode.HALF_UP),
                descuento.setScale(ESCALA, RoundingMode.HALF_UP),
                subtotal.subtract(descuento).setScale(ESCALA, RoundingMode.HALF_UP));
    }

    // ------------------------------------------------------------------ auxiliares

    private void validar(PromocionRequestDTO dto) throws MiException {
        if (dto == null) {
            throw new MiException("Faltan los datos de la promoción");
        }
        if (dto.codigo() == null || dto.codigo().isBlank()) {
            throw new MiException("El código es obligatorio");
        }
        if (dto.descripcion() == null || dto.descripcion().isBlank()) {
            throw new MiException("La descripción es obligatoria");
        }
        if (dto.tipo() == null) {
            throw new MiException("Indicá el tipo de descuento");
        }
        if (dto.valor() == null || dto.valor().signum() <= 0) {
            throw new MiException("El valor del descuento debe ser mayor a cero");
        }
        if (dto.tipo() == TipoPromocion.PORCENTAJE
                && dto.valor().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new MiException("Un descuento porcentual no puede superar el 100%");
        }
        if (dto.fechaDesde() == null) {
            throw new MiException("La fecha desde es obligatoria");
        }
        if (dto.fechaHasta() != null && dto.fechaHasta().isBefore(dto.fechaDesde())) {
            throw new MiException("La fecha hasta no puede ser anterior a la fecha desde");
        }
    }

    private void copiar(Promocion promocion, PromocionRequestDTO dto, String codigo) {
        promocion.setCodigo(codigo);
        promocion.setDescripcion(dto.descripcion().trim());
        promocion.setTipo(dto.tipo());
        promocion.setValor(dto.valor().setScale(ESCALA, RoundingMode.HALF_UP));
        promocion.setFechaDesde(dto.fechaDesde());
        promocion.setFechaHasta(dto.fechaHasta());
        promocion.setActiva(dto.activa() == null || dto.activa());
        promocion.setUsoUnico(Boolean.TRUE.equals(dto.usoUnico()));
        promocion.setMinimoCompra(dto.minimoCompra() == null ? null : dto.minimoCompra().setScale(ESCALA, RoundingMode.HALF_UP));
    }

    private static String normalizar(String codigo) {
        return codigo == null ? null : codigo.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private PromocionResponseDTO convertir(Promocion promocion) {
        return new PromocionResponseDTO(promocion.getId(), promocion.getCodigo(), promocion.getDescripcion(),
                promocion.getTipo(), promocion.getValor(), promocion.getFechaDesde(), promocion.getFechaHasta(),
                promocion.isActiva(), promocion.isUsoUnico(), promocion.getMinimoCompra(),
                promocion.vigente(LocalDate.now()), promocion.isEliminado());
    }
}
