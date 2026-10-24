package com.tienda.carrito.business.logic.service;

import com.tienda.carrito.business.domain.entity.Articulo;
import com.tienda.carrito.business.domain.entity.Imagen;
import com.tienda.carrito.business.logic.error.ErrorServiceException;
import com.tienda.carrito.business.percistence.repository.ArticuloRepository;
import com.tienda.carrito.dto.ArchivoImagen;
import com.tienda.carrito.dto.ArticuloCatalogoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Reglas de Articulo: nombre obligatorio (<=150), precio > 0, proveedor existente, al menos UNA imagen
 * (multiplicidad 1...* del diagrama; se exige al crear y no se puede quitar la ultima).
 *
 * Solo conoce ArticuloRepository. Para Proveedor e Imagen usa ProveedorService e ImagenService.
 */
@Service
public class ArticuloService extends BaseService<Articulo, String> {

    private static final double PRECIO_MAXIMO = 99_999_999.99;

    private final ArticuloRepository articuloRepository;
    private final ProveedorService proveedorService;
    private final ImagenService imagenService;

    public ArticuloService(ArticuloRepository articuloRepository,
                           ProveedorService proveedorService,
                           ImagenService imagenService) {
        super(articuloRepository);
        this.articuloRepository = articuloRepository;
        this.proveedorService = proveedorService;
        this.imagenService = imagenService;
    }

    @Override
    protected void validar(BaseUseCaseService useCase, Articulo articulo) throws ErrorServiceException {
        if (useCase == BaseUseCaseService.BAJA) {
            return;
        }
        if (vacio(articulo.getNombre())) {
            throw new ErrorServiceException("El nombre del artículo es obligatorio.");
        }
        if (articulo.getNombre().trim().length() > 150) {
            throw new ErrorServiceException("El nombre del artículo no puede superar los 150 caracteres.");
        }
        if (articulo.getPrecio() <= 0 || articulo.getPrecio() > PRECIO_MAXIMO) {
            throw new ErrorServiceException("El precio debe ser mayor a 0 y menor a 100.000.000.");
        }
        if (articulo.getProveedor() == null || vacio(articulo.getProveedor().getId())) {
            throw new ErrorServiceException("Seleccioná un proveedor.");
        }
        proveedorService.obtener(articulo.getProveedor().getId())
                .orElseThrow(() -> new ErrorServiceException("El proveedor seleccionado no existe."));
    }

    @Override
    protected void preAlta(Articulo articulo) throws ErrorServiceException {
        normalizar(articulo);
        articulo.setImagenes(new ArrayList<>()); // las imagenes solo entran por agregarImagenes()
    }

    /** En modificar() el articulo recibido reemplaza al guardado: se conservan sus imagenes. */
    @Override
    protected void preModificacion(Articulo articuloNuevo) throws ErrorServiceException {
        Articulo actual = obtenerEntidad(articuloNuevo.getId());
        normalizar(articuloNuevo);
        articuloNuevo.setImagenes(new ArrayList<>(actual.getImagenes()));
    }

    private void normalizar(Articulo articulo) throws ErrorServiceException {
        articulo.setNombre(articulo.getNombre().trim());
        articulo.setPrecio(Math.round(articulo.getPrecio() * 100.0) / 100.0);
        articulo.setProveedor(proveedorService.obtenerEntidad(articulo.getProveedor().getId()));
    }

    // ===================== Casos de uso propios de Articulo =====================

    /** Alta del articulo con sus imagenes en UNA transaccion (si algo falla no se guarda nada). */
    @Transactional(rollbackFor = Exception.class)
    public Articulo altaConImagenes(Articulo articulo, List<ArchivoImagen> archivos) throws ErrorServiceException {
        if (archivos == null || archivos.isEmpty()) {
            throw new ErrorServiceException("El artículo debe tener al menos una imagen.");
        }
        Articulo creado = alta(articulo);
        agregarImagenes(creado, archivos);
        return creado;
    }

    /** Modificacion del articulo; las imagenes nuevas (si hay) se SUMAN a las existentes. */
    @Transactional(rollbackFor = Exception.class)
    public Articulo modificarConImagenes(String id, Articulo datos, List<ArchivoImagen> archivos) throws ErrorServiceException {
        Articulo modificado = modificar(id, datos)
                .orElseThrow(() -> new ErrorServiceException("No se encontró el artículo a modificar."));
        if (archivos != null && !archivos.isEmpty()) {
            agregarImagenes(modificado, archivos);
        }
        return modificado;
    }

    /** Baja logica de una imagen. No permite dejar al articulo sin imagenes. */
    @Transactional(rollbackFor = Exception.class)
    public void quitarImagen(String articuloId, String imagenId) throws ErrorServiceException {
        Articulo articulo = obtenerEntidad(articuloId);
        List<Imagen> activas = articulo.getImagenesActivas();
        Imagen objetivo = activas.stream()
                .filter(i -> i.getId().equals(imagenId))
                .findFirst()
                .orElseThrow(() -> new ErrorServiceException("La imagen no pertenece a este artículo."));
        if (activas.size() <= 1) {
            throw new ErrorServiceException("El artículo debe conservar al menos una imagen.");
        }
        imagenService.baja(objetivo.getId());
    }

    /** Datos livianos para el catalogo (no carga los bytes de las imagenes). */
    @Transactional(readOnly = true)
    public List<ArticuloCatalogoDTO> listarParaCatalogo() throws ErrorServiceException {
        List<ArticuloCatalogoDTO> catalogo = new ArrayList<>();
        for (Articulo a : listarActivos()) {
            String imagenId = imagenService.listarIdsPorArticulo(a.getId()).stream().findFirst().orElse(null);
            String proveedor = a.getProveedor() != null ? a.getProveedor().getNombre() : "";
            catalogo.add(new ArticuloCatalogoDTO(a.getId(), a.getNombre(), a.getPrecio(), proveedor, imagenId));
        }
        return catalogo;
    }

    private void agregarImagenes(Articulo articulo, List<ArchivoImagen> archivos) throws ErrorServiceException {
        for (ArchivoImagen archivo : archivos) {
            Imagen imagen = new Imagen();
            imagen.setNombre(archivo.nombre());
            imagen.setMime(archivo.mime());
            imagen.setContenido(archivo.contenido());
            articulo.getImagenes().add(imagenService.alta(imagen)); // ImagenService valida cada archivo
        }
        articuloRepository.save(articulo);
    }
}
