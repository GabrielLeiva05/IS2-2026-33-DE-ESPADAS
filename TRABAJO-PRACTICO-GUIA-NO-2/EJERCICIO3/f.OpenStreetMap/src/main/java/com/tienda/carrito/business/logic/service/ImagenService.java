package com.tienda.carrito.business.logic.service;

import com.tienda.carrito.business.domain.entity.Imagen;
import com.tienda.carrito.business.logic.error.ErrorServiceException;
import com.tienda.carrito.business.percistence.repository.ImagenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Reglas de Imagen: nombre obligatorio; solo PNG/JPEG/GIF/WEBP (SVG excluido: puede contener scripts);
 * maximo 2 MB; el contenido real debe coincidir con el tipo declarado (se revisan los "magic bytes").
 */
@Service
public class ImagenService extends BaseService<Imagen, String> {

    private static final Set<String> MIMES_PERMITIDOS = Set.of("image/png", "image/jpeg", "image/gif", "image/webp");
    private static final int TAMANO_MAXIMO_BYTES = 2 * 1024 * 1024;

    private final ImagenRepository imagenRepository;

    public ImagenService(ImagenRepository imagenRepository) {
        super(imagenRepository);
        this.imagenRepository = imagenRepository;
    }

    @Override
    protected void validar(BaseUseCaseService useCase, Imagen imagen) throws ErrorServiceException {
        if (useCase == BaseUseCaseService.BAJA) {
            return;
        }
        if (vacio(imagen.getNombre())) {
            throw new ErrorServiceException("La imagen debe tener un nombre.");
        }
        if (imagen.getMime() == null || !MIMES_PERMITIDOS.contains(imagen.getMime().toLowerCase())) {
            throw new ErrorServiceException("Formato de imagen no permitido. Usá PNG, JPG, GIF o WEBP.");
        }
        byte[] contenido = imagen.getContenido();
        if (contenido == null || contenido.length == 0) {
            throw new ErrorServiceException("La imagen \"" + imagen.getNombre() + "\" está vacía.");
        }
        if (contenido.length > TAMANO_MAXIMO_BYTES) {
            throw new ErrorServiceException("La imagen \"" + imagen.getNombre() + "\" supera los 2 MB.");
        }
        if (!coincideConFirma(imagen.getMime().toLowerCase(), contenido)) {
            throw new ErrorServiceException("El archivo \"" + imagen.getNombre() + "\" no es una imagen válida.");
        }
    }

    @Override
    protected void preAlta(Imagen imagen) {
        imagen.setMime(imagen.getMime().toLowerCase());
        if (imagen.getNombre().length() > 255) {
            imagen.setNombre(imagen.getNombre().substring(0, 255));
        }
    }

    /** Ids de las imagenes activas de un articulo (sin traer los bytes). */
    @Transactional(readOnly = true)
    public List<String> listarIdsPorArticulo(String articuloId) {
        return imagenRepository.listarIdsActivosPorArticulo(articuloId);
    }

    private boolean coincideConFirma(String mime, byte[] b) {
        switch (mime) {
            case "image/png":
                return b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
            case "image/jpeg":
                return b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
            case "image/gif":
                return b.length > 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8';
            case "image/webp":
                return b.length > 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                        && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
            default:
                return false;
        }
    }
}
