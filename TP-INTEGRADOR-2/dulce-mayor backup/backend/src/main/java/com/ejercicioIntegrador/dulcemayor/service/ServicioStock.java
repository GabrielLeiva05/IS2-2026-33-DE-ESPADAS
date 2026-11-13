package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.DisponibilidadStockDTO;
import com.ejercicioIntegrador.dulcemayor.dto.StockRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.StockResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.OperacionNoPermitidaException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Producto;
import com.ejercicioIntegrador.dulcemayor.model.Stock;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioStock;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseUseCaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Stock centralizado del almacén (un registro por producto).
 * La cantidad solo cambia con reponerStock y descontarStock; el alta registra el stock inicial.
 * descontarStock es la operación que usará el módulo de Pedidos al facturar.
 */
@Service
public class ServicioStock extends BaseService<Stock, StockRequestDTO, StockResponseDTO> {

    private final RepositorioStock repositorioStock;
    private final ServicioProducto servicioProducto;

    public ServicioStock(RepositorioStock repositorioStock, ServicioProducto servicioProducto) {
        super(repositorioStock);
        this.repositorioStock = repositorioStock;
        this.servicioProducto = servicioProducto;
    }

    // ------------------------------------------------------------------
    // Pasos del Template Method
    // ------------------------------------------------------------------

    @Override
    protected Stock crearEntidad(StockRequestDTO dto) throws MiException {
        Stock stock = new Stock();
        stock.setProducto(obtenerReferencia(servicioProducto, dto.idProducto(), "el producto"));
        stock.setCantidadActual(dto.cantidadInicial() == null ? 0 : dto.cantidadInicial());
        stock.setStockMinimo(dto.stockMinimo() == null ? 0 : dto.stockMinimo());
        return stock;
    }

    @Override
    protected void actualizarEntidad(Stock stock, StockRequestDTO dto) throws MiException {
        throw new OperacionNoPermitidaException(
                "El stock no se modifica directamente: use las operaciones de reponer o descontar");
    }

    @Override
    protected void validar(BaseUseCaseService casoDeUso, Stock stock) throws MiException {
        if (casoDeUso == BaseUseCaseService.BAJA) {
            throw new OperacionNoPermitidaException("El stock de un producto no se puede dar de baja");
        }
        if (stock.getCantidadActual() < 0) {
            throw new MiException("La cantidad inicial no puede ser negativa");
        }
        if (stock.getStockMinimo() < 0) {
            throw new MiException("El stock mínimo no puede ser negativo");
        }
        if (casoDeUso == BaseUseCaseService.ALTA
                && repositorioStock.findByProducto_IdAndEliminadoFalse(stock.getProducto().getId()).isPresent()) {
            throw new MiException("El producto " + stock.getProducto().getNombre() + " ya tiene stock registrado");
        }
    }

    @Override
    protected void preAlta(Stock stock) {
        stock.setFechaActualizacion(LocalDateTime.now());
    }

    @Override
    public StockResponseDTO convertirAResponse(Stock stock) {
        Producto producto = stock.getProducto();
        return new StockResponseDTO(stock.getId(), producto.getId(), producto.getCodigo(), producto.getNombre(),
                stock.getCantidadActual(), stock.getStockMinimo(),
                stock.getCantidadActual() < stock.getStockMinimo(), stock.getFechaActualizacion());
    }

    @Override
    protected String nombreEntidad() {
        return "el stock";
    }

    // ------------------------------------------------------------------
    // Operaciones propias (diagrama de clases)
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public StockResponseDTO buscarStockPorProducto(String idProducto) throws MiException {
        return convertirAResponse(buscarEntidadPorProducto(idProducto));
    }

    /** Cantidad disponible del producto; 0 si todavía no tiene stock registrado. */
    @Transactional(readOnly = true)
    public int cantidadDisponible(String idProducto) {
        return repositorioStock.findByProducto_IdAndEliminadoFalse(idProducto)
                .map(Stock::getCantidadActual)
                .orElse(0);
    }

    @Transactional(readOnly = true)
    public boolean hayStockSuficiente(String idProducto, Integer cantidad) throws MiException {
        validarCantidad(cantidad);
        return cantidadDisponible(idProducto) >= cantidad;
    }

    @Transactional(readOnly = true)
    public DisponibilidadStockDTO consultarDisponibilidad(String idProducto, Integer cantidad) throws MiException {
        servicioProducto.obtenerEntidad(idProducto);
        boolean disponible = hayStockSuficiente(idProducto, cantidad);
        return new DisponibilidadStockDTO(idProducto, cantidad, cantidadDisponible(idProducto), disponible);
    }

    @Transactional(rollbackFor = Exception.class)
    public StockResponseDTO descontarStock(String idProducto, Integer cantidad) throws MiException {
        validarCantidad(cantidad);
        Stock stock = bloquearPorProducto(idProducto);
        if (stock.getCantidadActual() < cantidad) {
            throw new MiException("Stock insuficiente de " + stock.getProducto().getNombre()
                    + ": disponible " + stock.getCantidadActual() + ", solicitado " + cantidad);
        }
        stock.setCantidadActual(stock.getCantidadActual() - cantidad);
        stock.setFechaActualizacion(LocalDateTime.now());
        return convertirAResponse(repositorioStock.save(stock));
    }

    @Transactional(rollbackFor = Exception.class)
    public StockResponseDTO reponerStock(String idProducto, Integer cantidad) throws MiException {
        validarCantidad(cantidad);
        Stock stock = bloquearPorProducto(idProducto);
        stock.setCantidadActual(stock.getCantidadActual() + cantidad);
        stock.setFechaActualizacion(LocalDateTime.now());
        return convertirAResponse(repositorioStock.save(stock));
    }

    /** Productos cuyo stock llegó a cero (dejan de mostrarse en el catálogo público). */
    @Transactional(readOnly = true)
    public List<StockResponseDTO> listarProductoSinStock() {
        return convertirAResponse(repositorioStock.findByEliminadoFalseAndCantidadActualLessThanEqual(0));
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private Stock buscarEntidadPorProducto(String idProducto) throws MiException {
        if (estaVacio(idProducto)) {
            throw new MiException("Debe indicar el producto");
        }
        return repositorioStock.findByProducto_IdAndEliminadoFalse(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto no tiene stock registrado"));
    }

    private Stock bloquearPorProducto(String idProducto) throws MiException {
        if (estaVacio(idProducto)) {
            throw new MiException("Debe indicar el producto");
        }
        return repositorioStock.buscarParaActualizarPorProducto(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto no tiene stock registrado"));
    }

    private static void validarCantidad(Integer cantidad) throws MiException {
        if (cantidad == null || cantidad <= 0) {
            throw new MiException("La cantidad debe ser mayor a cero");
        }
    }

    /** Stock agrupado por categoría para el panel (gráfico de torta, HU-43). La agregación la hace la base. */
    @Transactional(readOnly = true)
    public java.util.List<com.ejercicioIntegrador.dulcemayor.dto.StockCategoriaDTO> stockPorCategoria() {
        return repositorioStock.stockPorCategoria().stream()
                .map(fila -> new com.ejercicioIntegrador.dulcemayor.dto.StockCategoriaDTO((String) fila[0],
                        ((Number) fila[1]).longValue(), ((Number) fila[2]).longValue()))
                .toList();
    }
}
