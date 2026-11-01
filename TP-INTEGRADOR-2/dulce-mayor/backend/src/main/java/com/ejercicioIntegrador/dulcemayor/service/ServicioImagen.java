package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.ImagenContenidoDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ImagenRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ImagenResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Imagen;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioImagen;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseUseCaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reutiliza las validaciones de ServicioImagen del Integrador 1 (nombre, contenido y tipo obligatorios)
 * y agrega las del nuevo diseño (mime de imagen y tamaño máximo).
 */
@Service
public class ServicioImagen extends BaseService<Imagen, ImagenRequestDTO, ImagenResponseDTO> {

    /** Mismo límite que la subida de archivos configurada en application.properties. */
    static final long TAMANIO_MAXIMO_BYTES = 10L * 1024 * 1024;
    private static final int LARGO_MAXIMO_NOMBRE = 100;

    public ServicioImagen(RepositorioImagen repositorioImagen) {
        super(repositorioImagen);
    }

    @Override
    protected Imagen crearEntidad(ImagenRequestDTO dto) {
        Imagen imagen = new Imagen();
        actualizarEntidad(imagen, dto);
        return imagen;
    }

    @Override
    protected void actualizarEntidad(Imagen imagen, ImagenRequestDTO dto) {
        imagen.setNombre(normalizarTexto(dto.nombre()));
        imagen.setMime(normalizarTexto(dto.mime()));
        imagen.setContenido(dto.contenido());
        imagen.setTipoImagen(dto.tipoImagen());
    }

    @Override
    protected void validar(BaseUseCaseService casoDeUso, Imagen imagen) throws MiException {
        if (casoDeUso == BaseUseCaseService.BAJA) {
            return;
        }
        if (estaVacio(imagen.getNombre())) {
            throw new MiException("El nombre de la imagen no puede ser nulo o vacío");
        }
        if (imagen.getNombre().length() > LARGO_MAXIMO_NOMBRE) {
            throw new MiException("El nombre de la imagen no puede superar los " + LARGO_MAXIMO_NOMBRE + " caracteres");
        }
        if (estaVacio(imagen.getMime()) || !imagen.getMime().toLowerCase().startsWith("image/")) {
            throw new MiException("El archivo debe ser una imagen (tipo MIME image/...)");
        }
        if (imagen.getContenido() == null || imagen.getContenido().length == 0) {
            throw new MiException("El contenido de la imagen no puede ser nulo o vacío");
        }
        if (imagen.getContenido().length > TAMANIO_MAXIMO_BYTES) {
            throw new MiException("La imagen no puede superar los 10 MB");
        }
        if (imagen.getTipoImagen() == null) {
            throw new MiException("El tipo de imagen no puede ser nulo");
        }
    }

    @Override
    public ImagenResponseDTO convertirAResponse(Imagen imagen) {
        long tamanio = imagen.getContenido() == null ? 0 : imagen.getContenido().length;
        return new ImagenResponseDTO(imagen.getId(), imagen.getNombre(), imagen.getMime(), imagen.getTipoImagen(),
                tamanio, imagen.isEliminado());
    }

    @Override
    protected String nombreEntidad() {
        return "la imagen";
    }

    @Transactional(readOnly = true)
    public ImagenContenidoDTO obtenerContenido(String id) throws MiException {
        Imagen imagen = obtenerEntidad(id);
        return new ImagenContenidoDTO(imagen.getNombre(), imagen.getMime(), imagen.getContenido());
    }
}
