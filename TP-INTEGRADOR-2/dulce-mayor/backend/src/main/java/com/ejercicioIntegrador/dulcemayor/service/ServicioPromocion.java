package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.PromocionRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PromocionResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ReferenciaDTO;
import com.ejercicioIntegrador.dulcemayor.dto.TramoVolumenDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoValorDescuento;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.CodigoDescuento;
import com.ejercicioIntegrador.dulcemayor.model.Producto;
import com.ejercicioIntegrador.dulcemayor.model.Promocion;
import com.ejercicioIntegrador.dulcemayor.model.PromocionPorcentual;
import com.ejercicioIntegrador.dulcemayor.model.PromocionVolumen;
import com.ejercicioIntegrador.dulcemayor.model.SubCategoria;
import com.ejercicioIntegrador.dulcemayor.model.TramoVolumen;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPromocion;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseUseCaseService;
import com.ejercicioIntegrador.dulcemayor.service.promociones.CalculadorDescuentos;
import com.ejercicioIntegrador.dulcemayor.service.promociones.OfertaCatalogo;
import com.ejercicioIntegrador.dulcemayor.service.promociones.RenglonDescuento;
import com.ejercicioIntegrador.dulcemayor.service.promociones.ResultadoDescuentos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Promociones y códigos de descuento (HU-22). ABM con el Template Method de BaseService y, para los pedidos:
 *  - calcular(): aplica las promociones automáticas vigentes y el código (CalculadorDescuentos, con BigDecimal);
 *  - registrarUsoCodigo()/liberarUsoCodigo(): cuenta los usos del código con el registro bloqueado.
 * Los productos y subcategorías se resuelven a través de sus servicios, sin acceder a sus repositorios.
 */
@Service
public class ServicioPromocion extends BaseService<Promocion, PromocionRequestDTO, PromocionResponseDTO> {

    private static final Pattern FORMATO_CODIGO = Pattern.compile("[A-Z0-9_-]{3,30}");
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final java.time.format.DateTimeFormatter FECHA = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RepositorioPromocion repositorioPromocion;
    private final ServicioProducto servicioProducto;
    private final ServicioSubCategoria servicioSubCategoria;

    public ServicioPromocion(RepositorioPromocion repositorioPromocion, ServicioProducto servicioProducto,
                             ServicioSubCategoria servicioSubCategoria) {
        super(repositorioPromocion);
        this.repositorioPromocion = repositorioPromocion;
        this.servicioProducto = servicioProducto;
        this.servicioSubCategoria = servicioSubCategoria;
    }

    // ------------------------------------------------------------------
    // Pasos del Template Method
    // ------------------------------------------------------------------

    @Override
    protected Promocion crearEntidad(PromocionRequestDTO dto) throws MiException {
        if (dto.tipo() == null) {
            throw new MiException("Debe indicar el tipo de promoción");
        }
        Promocion promocion = switch (dto.tipo()) {
            case PORCENTAJE_PRODUCTO -> new PromocionPorcentual();
            case VOLUMEN -> new PromocionVolumen();
            case CODIGO -> new CodigoDescuento();
        };
        copiarDatos(promocion, dto);
        return promocion;
    }

    @Override
    protected void actualizarEntidad(Promocion promocion, PromocionRequestDTO dto) throws MiException {
        if (dto.tipo() != null && dto.tipo() != promocion.getTipo()) {
            throw new MiException("No se puede cambiar el tipo de una promoción: creá una nueva");
        }
        copiarDatos(promocion, dto);
    }

    private void copiarDatos(Promocion promocion, PromocionRequestDTO dto) throws MiException {
        promocion.setNombre(normalizarTexto(dto.nombre()));
        promocion.setDescripcion(estaVacio(dto.descripcion()) ? null : dto.descripcion().trim());
        promocion.setFechaInicio(dto.fechaInicio());
        promocion.setFechaFin(dto.fechaFin());
        promocion.setActiva(dto.activa() == null || dto.activa());
        Set<Producto> productos = new LinkedHashSet<>();
        for (String id : sinRepetidos(dto.idsProductos())) {
            productos.add(obtenerReferencia(servicioProducto, id, "el producto"));
        }
        Set<SubCategoria> subCategorias = new LinkedHashSet<>();
        for (String id : sinRepetidos(dto.idsSubCategorias())) {
            subCategorias.add(obtenerReferencia(servicioSubCategoria, id, "la subcategoría"));
        }
        promocion.getProductos().clear();
        promocion.getProductos().addAll(productos);
        promocion.getSubCategorias().clear();
        promocion.getSubCategorias().addAll(subCategorias);

        if (promocion instanceof PromocionPorcentual porcentual) {
            porcentual.setPorcentaje(dto.porcentaje());
        } else if (promocion instanceof PromocionVolumen volumen) {
            List<TramoVolumen> tramos = new ArrayList<>();
            if (dto.tramos() != null) {
                for (TramoVolumenDTO t : dto.tramos()) {
                    if (t == null || t.cantidadDesde() == null || t.porcentaje() == null) {
                        throw new MiException("Cada tramo necesita la cantidad desde y el porcentaje");
                    }
                    tramos.add(new TramoVolumen(t.cantidadDesde(), t.cantidadHasta(), t.porcentaje()));
                }
            }
            tramos.sort(Comparator.comparingInt(TramoVolumen::getCantidadDesde));
            volumen.reemplazarTramos(tramos);
        } else if (promocion instanceof CodigoDescuento codigo) {
            codigo.setCodigo(dto.codigo() == null ? null : dto.codigo().trim().toUpperCase(Locale.ROOT));
            codigo.setTipoValor(dto.tipoValor());
            codigo.setValor(dto.valor());
            codigo.setMontoMinimo(dto.montoMinimo());
            codigo.setUsosMaximos(dto.usosMaximos());
            codigo.setAcumulable(dto.acumulable() == null || dto.acumulable());
        }
    }

    private static List<String> sinRepetidos(List<String> ids) {
        return ids == null ? List.of() : ids.stream().filter(id -> id != null && !id.isBlank()).distinct().toList();
    }

    @Override
    protected void validar(BaseUseCaseService casoDeUso, Promocion promocion) throws MiException {
        if (casoDeUso == BaseUseCaseService.BAJA) {
            return;
        }
        if (estaVacio(promocion.getNombre()) || promocion.getNombre().length() > 80) {
            throw new MiException("El nombre de la promoción es obligatorio (hasta 80 caracteres)");
        }
        if (promocion.getDescripcion() != null && promocion.getDescripcion().length() > 250) {
            throw new MiException("La descripción admite hasta 250 caracteres");
        }
        if (promocion.getFechaInicio() == null || promocion.getFechaFin() == null) {
            throw new MiException("Indicá la fecha de inicio y la de finalización");
        }
        if (promocion.getFechaFin().isBefore(promocion.getFechaInicio())) {
            throw new MiException("La fecha de finalización no puede ser anterior a la de inicio");
        }
        if (promocion instanceof PromocionPorcentual porcentual) {
            exigirAlcance(promocion);
            validarPorcentaje(porcentual.getPorcentaje(), "El porcentaje de descuento");
        } else if (promocion instanceof PromocionVolumen volumen) {
            exigirAlcance(promocion);
            validarTramos(volumen.getTramos());
        } else if (promocion instanceof CodigoDescuento codigo) {
            validarCodigo(codigo);
        }
    }

    private static void exigirAlcance(Promocion promocion) throws MiException {
        if (!promocion.tieneAlcanceRestringido()) {
            throw new MiException("Elegí al menos un producto o una subcategoría para la promoción");
        }
    }

    private static void validarPorcentaje(BigDecimal porcentaje, String descripcion) throws MiException {
        if (porcentaje == null || porcentaje.signum() <= 0 || porcentaje.compareTo(CIEN) > 0
                || porcentaje.stripTrailingZeros().scale() > 2) {
            throw new MiException(descripcion + " debe ser mayor a 0 y hasta 100, con hasta 2 decimales");
        }
    }

    static void validarTramos(List<TramoVolumen> tramos) throws MiException {
        if (tramos.isEmpty()) {
            throw new MiException("Cargá al menos un tramo de cantidades con su porcentaje");
        }
        Integer hastaAnterior = 0;
        for (int i = 0; i < tramos.size(); i++) {
            TramoVolumen tramo = tramos.get(i);
            validarPorcentaje(tramo.getPorcentaje(), "El porcentaje de cada tramo");
            if (tramo.getCantidadDesde() < 1) {
                throw new MiException("Las cantidades de los tramos empiezan en 1");
            }
            if (tramo.getCantidadHasta() != null && tramo.getCantidadHasta() < tramo.getCantidadDesde()) {
                throw new MiException("En cada tramo, «hasta» no puede ser menor que «desde»");
            }
            if (hastaAnterior == null || tramo.getCantidadDesde() <= hastaAnterior) {
                throw new MiException("Los tramos no pueden superponerse (revisá el tramo que empieza en "
                        + tramo.getCantidadDesde() + ")");
            }
            if (tramo.getCantidadHasta() == null && i < tramos.size() - 1) {
                throw new MiException("Solo el último tramo puede quedar sin cantidad máxima");
            }
            hastaAnterior = tramo.getCantidadHasta();
        }
    }

    private void validarCodigo(CodigoDescuento codigo) throws MiException {
        if (codigo.getCodigo() == null || !FORMATO_CODIGO.matcher(codigo.getCodigo()).matches()) {
            throw new MiException("El código debe tener de 3 a 30 letras, números, guiones o guiones bajos (sin espacios)");
        }
        if (codigo.getTipoValor() == null) {
            throw new MiException("Indicá si el código descuenta un porcentaje o un importe fijo");
        }
        if (codigo.getTipoValor() == TipoValorDescuento.PORCENTAJE) {
            validarPorcentaje(codigo.getValor(), "El porcentaje del código");
        } else if (codigo.getValor() == null || codigo.getValor().signum() <= 0) {
            throw new MiException("El importe del código debe ser mayor a cero");
        }
        if (codigo.getMontoMinimo() != null && codigo.getMontoMinimo().signum() < 0) {
            throw new MiException("El monto mínimo no puede ser negativo");
        }
        if (codigo.getUsosMaximos() != null && codigo.getUsosMaximos() < 1) {
            throw new MiException("La cantidad máxima de usos debe ser al menos 1 (o dejarla vacía: sin límite)");
        }
        if (codigo.getUsosMaximos() != null && codigo.getUsosMaximos() < codigo.getUsosRealizados()) {
            throw new MiException("El código ya se usó " + codigo.getUsosRealizados()
                    + " veces: el máximo no puede ser menor");
        }
    }

    @Override
    protected void preAlta(Promocion promocion) throws MiException {
        verificarCodigoUnico(promocion);
    }

    @Override
    protected void preModificacion(Promocion promocion) throws MiException {
        verificarCodigoUnico(promocion);
    }

    private void verificarCodigoUnico(Promocion promocion) throws MiException {
        if (promocion instanceof CodigoDescuento codigo && repositorioPromocion.buscarCodigo(codigo.getCodigo())
                .filter(otro -> !otro.getId().equals(codigo.getId())).isPresent()) {
            throw new MiException("Ya existe un código " + codigo.getCodigo());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> listarActivos() {
        return convertirAResponse(repositorioPromocion.findByEliminadoFalseOrderByFechaInicioDesc());
    }

    @Override
    public PromocionResponseDTO convertirAResponse(Promocion p) {
        BigDecimal porcentaje = p instanceof PromocionPorcentual pp ? pp.getPorcentaje() : null;
        List<TramoVolumenDTO> tramos = p instanceof PromocionVolumen pv
                ? pv.getTramos().stream().map(t -> new TramoVolumenDTO(t.getCantidadDesde(), t.getCantidadHasta(),
                t.getPorcentaje())).toList()
                : List.of();
        CodigoDescuento c = p instanceof CodigoDescuento cd ? cd : null;
        return new PromocionResponseDTO(p.getId(), p.getTipo(), p.getNombre(), p.getDescripcion(), p.getFechaInicio(),
                p.getFechaFin(), p.isActiva(), p.vigenteEn(LocalDate.now()),
                p.getProductos().stream().map(x -> new ReferenciaDTO(x.getId(), x.getNombre())).toList(),
                p.getSubCategorias().stream().map(x -> new ReferenciaDTO(x.getId(), x.getNombre())).toList(),
                porcentaje, tramos,
                c == null ? null : c.getCodigo(), c == null ? null : c.getTipoValor(), c == null ? null : c.getValor(),
                c == null ? null : c.getMontoMinimo(), c == null ? null : c.getUsosMaximos(),
                c == null ? 0 : c.getUsosRealizados(), c == null || c.isAcumulable(), resumen(p), p.isEliminado());
    }

    @Override
    protected String nombreEntidad() {
        return "la promoción";
    }

    // ------------------------------------------------------------------
    // Aplicación en pedidos, presupuestos y catálogo
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Promocion> automaticasVigentes() {
        return repositorioPromocion.automaticasVigentes(LocalDate.now());
    }

    /**
     * Valida que el código exista, esté activo, vigente y con usos disponibles. Las condiciones que dependen del
     * pedido (monto mínimo, productos alcanzados) las controla el cálculo.
     */
    @Transactional(readOnly = true)
    public CodigoDescuento codigoAplicable(String texto) throws MiException {
        if (estaVacio(texto)) {
            throw new MiException("Ingresá el código de descuento");
        }
        String normalizado = texto.trim().toUpperCase(Locale.ROOT);
        CodigoDescuento codigo = repositorioPromocion.buscarCodigo(normalizado)
                .orElseThrow(() -> new MiException("El código " + normalizado + " no existe"));
        LocalDate hoy = LocalDate.now();
        if (!codigo.isActiva()) {
            throw new MiException("El código " + normalizado + " está desactivado");
        }
        if (hoy.isBefore(codigo.getFechaInicio())) {
            throw new MiException("El código " + normalizado + " todavía no está vigente (desde el "
                    + codigo.getFechaInicio().format(FECHA) + ")");
        }
        if (hoy.isAfter(codigo.getFechaFin())) {
            throw new MiException("El código " + normalizado + " venció el " + codigo.getFechaFin().format(FECHA));
        }
        if (codigo.usosAgotados()) {
            throw new MiException("El código " + normalizado + " ya alcanzó su cantidad máxima de usos");
        }
        return codigo;
    }

    /** Descuentos de un pedido o presupuesto: promociones automáticas vigentes más el código (si se indicó). */
    @Transactional(readOnly = true)
    public ResultadoDescuentos calcular(List<RenglonDescuento> renglones, String codigo) throws MiException {
        CodigoDescuento aplicable = estaVacio(codigo) ? null : codigoAplicable(codigo);
        return CalculadorDescuentos.calcular(renglones, automaticasVigentes(), aplicable);
    }

    /** Cuenta un uso del código con el registro bloqueado, para no superar el máximo con pedidos simultáneos. */
    @Transactional(rollbackFor = Exception.class)
    public void registrarUsoCodigo(String idCodigo) throws MiException {
        CodigoDescuento codigo = repositorioPromocion.bloquearCodigo(idCodigo)
                .orElseThrow(() -> new MiException("El código de descuento ya no existe"));
        if (codigo.usosAgotados()) {
            throw new MiException("El código " + codigo.getCodigo() + " ya alcanzó su cantidad máxima de usos");
        }
        codigo.setUsosRealizados(codigo.getUsosRealizados() + 1);
        repositorioPromocion.save(codigo);
    }

    /** Devuelve el uso del código cuando el pedido que lo usó se anula. */
    @Transactional(rollbackFor = Exception.class)
    public void liberarUsoCodigo(String idCodigo) {
        repositorioPromocion.bloquearCodigo(idCodigo).ifPresent(codigo -> {
            codigo.setUsosRealizados(Math.max(0, codigo.getUsosRealizados() - 1));
            repositorioPromocion.save(codigo);
        });
    }

    /** Promociones automáticas de un producto, para el catálogo (con las vigentes ya cargadas). */
    public static OfertaCatalogo ofertaPara(Producto producto, List<Promocion> vigentes) {
        BigDecimal mejorUnitario = BigDecimal.ZERO;
        List<String> textos = new ArrayList<>();
        for (Promocion promocion : vigentes) {
            if (!promocion.alcanza(producto)) {
                continue;
            }
            BigDecimal unitario = CalculadorDescuentos.porcentajeAutomatico(promocion,
                    new RenglonDescuento(producto, 1, BigDecimal.ONE));
            if (unitario.compareTo(mejorUnitario) > 0) {
                mejorUnitario = unitario;
            }
            textos.add(resumen(promocion));
        }
        return textos.isEmpty() ? OfertaCatalogo.NINGUNA : new OfertaCatalogo(mejorUnitario, List.copyOf(textos));
    }

    /** "15 % de descuento", "5 % de 10 a 19 · 10 % desde 20", "DULCE10: 10 % (compra mínima $ 50000)". */
    public static String resumen(Promocion promocion) {
        if (promocion instanceof PromocionPorcentual p) {
            return texto(p.getPorcentaje()) + " % de descuento";
        }
        if (promocion instanceof PromocionVolumen v) {
            List<String> partes = new ArrayList<>();
            for (TramoVolumen t : v.getTramos()) {
                String rango = t.getCantidadHasta() == null ? "desde " + t.getCantidadDesde()
                        : t.getCantidadDesde() == t.getCantidadHasta() ? "con " + t.getCantidadDesde()
                        : "de " + t.getCantidadDesde() + " a " + t.getCantidadHasta();
                partes.add(texto(t.getPorcentaje()) + " % " + rango + " bultos");
            }
            return String.join(" · ", partes);
        }
        if (promocion instanceof CodigoDescuento c) {
            String valor = c.getTipoValor() == TipoValorDescuento.PORCENTAJE ? texto(c.getValor()) + " %"
                    : "$ " + c.getValor().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
            String minimo = c.getMontoMinimo() == null || c.getMontoMinimo().signum() == 0 ? ""
                    : " (compra mínima $ " + c.getMontoMinimo().stripTrailingZeros().toPlainString() + ")";
            return c.getCodigo() + ": " + valor + minimo;
        }
        return promocion.getNombre();
    }

    private static String texto(BigDecimal valor) {
        return valor == null ? "?" : valor.stripTrailingZeros().toPlainString();
    }

}
