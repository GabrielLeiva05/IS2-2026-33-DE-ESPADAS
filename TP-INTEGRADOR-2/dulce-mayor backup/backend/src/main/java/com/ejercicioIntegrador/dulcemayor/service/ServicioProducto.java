package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.ProductoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ProductoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.Presentacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoImagen;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Categoria;
import com.ejercicioIntegrador.dulcemayor.model.Imagen;
import com.ejercicioIntegrador.dulcemayor.model.Producto;
import com.ejercicioIntegrador.dulcemayor.model.SubCategoria;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioProducto;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseUseCaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ServicioProducto extends BaseService<Producto, ProductoRequestDTO, ProductoResponseDTO> {

    private final RepositorioProducto repositorioProducto;
    private final ServicioSubCategoria servicioSubCategoria;
    private final ServicioImagen servicioImagen;

    public ServicioProducto(RepositorioProducto repositorioProducto, ServicioSubCategoria servicioSubCategoria,
                            ServicioImagen servicioImagen) {
        super(repositorioProducto);
        this.repositorioProducto = repositorioProducto;
        this.servicioSubCategoria = servicioSubCategoria;
        this.servicioImagen = servicioImagen;
    }

    // ------------------------------------------------------------------
    // Pasos del Template Method
    // ------------------------------------------------------------------

    @Override
    protected Producto crearEntidad(ProductoRequestDTO dto) throws MiException {
        Producto producto = new Producto();
        producto.setCodigo(normalizarTexto(dto.codigo()));
        copiarDatosModificables(producto, dto);
        producto.setImagenes(resolverImagenes(dto.idsImagen()));
        return producto;
    }

    @Override
    protected void actualizarEntidad(Producto producto, ProductoRequestDTO dto) throws MiException {
        String codigo = normalizarTexto(dto.codigo());
        if (codigo != null && !codigo.equalsIgnoreCase(producto.getCodigo())) {
            throw new MiException("El código de un producto no se puede modificar");
        }
        copiarDatosModificables(producto, dto);
        if (dto.idsImagen() != null) {
            List<Imagen> nuevas = resolverImagenes(dto.idsImagen());
            producto.getImagenes().clear();
            producto.getImagenes().addAll(nuevas);
        }
    }

    /**
     * Pedidos se consulta de forma diferida porque ServicioPedido ya depende de este servicio (evita el ciclo).
     * Se usa solo para proteger la baja.
     */
    private org.springframework.beans.factory.ObjectProvider<ServicioPedido> servicioPedido;

    @org.springframework.beans.factory.annotation.Autowired
    void setServicioPedido(org.springframework.beans.factory.ObjectProvider<ServicioPedido> servicioPedido) {
        this.servicioPedido = servicioPedido;
    }

    @Override
    protected void validar(BaseUseCaseService casoDeUso, Producto producto) throws MiException {
        if (casoDeUso == BaseUseCaseService.BAJA) {
            // Baja lógica: los pedidos y facturas históricos siguen apuntando al producto. No se permite si
            // todavía hay pedidos cargados o por entregar que lo incluyen (quedarían con un producto inexistente).
            ServicioPedido pedidos = servicioPedido == null ? null : servicioPedido.getIfAvailable();
            if (pedidos != null && pedidos.hayPedidosAbiertosConProducto(producto.getId())) {
                throw new MiException("No se puede dar de baja: el producto está en pedidos cargados o pendientes de entrega");
            }
            return;
        }
        if (estaVacio(producto.getCodigo())) {
            throw new MiException("El código es obligatorio");
        }
        if (estaVacio(producto.getNombre())) {
            throw new MiException("El nombre es obligatorio");
        }
        if (producto.getPresentacion() == null) {
            throw new MiException("Debe indicar la presentación (UNIDAD, PACK, DISPLAY, CAJA o BULTO)");
        }
        if (producto.getUnidadesPorPresentacion() < 1) {
            throw new MiException("Las unidades por presentación deben ser al menos 1");
        }
        if (producto.getPresentacion() == Presentacion.UNIDAD && producto.getUnidadesPorPresentacion() != 1) {
            throw new MiException("Un producto que se vende por unidad debe tener 1 unidad por presentación");
        }
        validarClasificacion(producto.getSubCategoria());
        validarImagenes(producto);

        Optional<Producto> mismoCodigo = repositorioProducto.findFirstByCodigoIgnoreCaseAndEliminadoFalse(producto.getCodigo());
        if (mismoCodigo.isPresent() && !Objects.equals(mismoCodigo.get().getId(), producto.getId())) {
            throw new MiException("Ya existe un producto con el código " + producto.getCodigo());
        }
    }

    @Override
    public ProductoResponseDTO convertirAResponse(Producto producto) {
        SubCategoria subCategoria = producto.getSubCategoria();
        Categoria categoria = subCategoria.getCategoria();
        return new ProductoResponseDTO(producto.getId(), producto.getCodigo(), producto.getNombre(),
                producto.getDescripcion(), producto.getPresentacion(), producto.getUnidadesPorPresentacion(),
                producto.isEnOferta(), subCategoria.getId(), subCategoria.getNombre(),
                categoria.getId(), categoria.getNombre(),
                producto.getImagenes().stream()
                        .filter(imagen -> !imagen.isEliminado())
                        .map(servicioImagen::convertirAResponse)
                        .toList(),
                producto.isEliminado());
    }

    @Override
    protected String nombreEntidad() {
        return "el producto";
    }

    // ------------------------------------------------------------------
    // Operaciones propias del producto (diagrama de clases)
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ProductoResponseDTO buscarProductoPorCodigo(String codigo) throws MiException {
        if (estaVacio(codigo)) {
            throw new MiException("Debe indicar el código a buscar");
        }
        return repositorioProducto.findFirstByCodigoIgnoreCaseAndEliminadoFalse(codigo.trim())
                .map(this::convertirAResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto con el código " + codigo));
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO buscarProductoPorNombre(String nombre) throws MiException {
        if (estaVacio(nombre)) {
            throw new MiException("Debe indicar el nombre a buscar");
        }
        return repositorioProducto.findFirstByNombreIgnoreCaseAndEliminadoFalse(nombre.trim())
                .map(this::convertirAResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto con el nombre " + nombre));
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarProductoEnOferta() {
        return convertirAResponse(repositorioProducto.findByEnOfertaTrueAndEliminadoFalse());
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private void copiarDatosModificables(Producto producto, ProductoRequestDTO dto) throws MiException {
        producto.setNombre(normalizarTexto(dto.nombre()));
        producto.setDescripcion(normalizarTexto(dto.descripcion()));
        producto.setPresentacion(dto.presentacion());
        producto.setUnidadesPorPresentacion(dto.unidadesPorPresentacion() == null ? 0 : dto.unidadesPorPresentacion());
        producto.setEnOferta(dto.enOferta());
        producto.setSubCategoria(obtenerReferencia(servicioSubCategoria, dto.idSubCategoria(), "la subcategoría"));
    }

    private List<Imagen> resolverImagenes(List<String> idsImagen) throws MiException {
        List<Imagen> imagenes = new ArrayList<>();
        if (idsImagen == null) {
            return imagenes;
        }
        for (String idImagen : new LinkedHashSet<>(idsImagen)) {
            imagenes.add(obtenerReferencia(servicioImagen, idImagen, "la imagen"));
        }
        return imagenes;
    }

    private void validarClasificacion(SubCategoria subCategoria) throws MiException {
        if (subCategoria == null || subCategoria.isEliminado()) {
            throw new MiException("Debe indicar una subcategoría activa");
        }
        if (subCategoria.getCategoria() == null || subCategoria.getCategoria().isEliminado()) {
            throw new MiException("La categoría de la subcategoría " + subCategoria.getNombre() + " está dada de baja");
        }
    }

    private void validarImagenes(Producto producto) throws MiException {
        List<Imagen> imagenes = producto.getImagenes();
        if (imagenes == null || imagenes.isEmpty()) {
            throw new MiException("El producto debe tener al menos una imagen");
        }
        for (Imagen imagen : imagenes) {
            if (imagen.isEliminado()) {
                throw new MiException("La imagen " + imagen.getNombre() + " está dada de baja");
            }
            if (imagen.getTipoImagen() != TipoImagen.PRODUCTO) {
                throw new MiException("La imagen " + imagen.getNombre() + " no es de tipo PRODUCTO");
            }
            // Cada imagen pertenece a un único producto (relación Producto 1 --> 1..* Imagen)
            Optional<Producto> otroDuenio = repositorioProducto.findByImagenes_Id(imagen.getId()).stream()
                    .filter(otro -> !Objects.equals(otro.getId(), producto.getId()))
                    .findFirst();
            if (otroDuenio.isPresent()) {
                throw new MiException("La imagen " + imagen.getNombre() + " ya está asignada al producto "
                        + otroDuenio.get().getCodigo());
            }
        }
    }
}
