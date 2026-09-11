package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoImagen;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Imagen;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioImagen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public class ServicioImagen {

    @Autowired
    private RepositorioImagen imagenRepositorio;
    public Imagen guardar(MultipartFile archivo, TipoImagen tipoImagen) throws MiException {
        if (archivo != null && !archivo.isEmpty()) {
            try {
                Imagen imagen = new Imagen();
                imagen.setMime(archivo.getContentType());
                imagen.setNombre(archivo.getOriginalFilename());
                imagen.setContenido(archivo.getBytes());
                imagen.setEliminado(false);
                imagen.setTipoImagen(tipoImagen);
                return imagenRepositorio.save(imagen);
            } catch (Exception e) {
                e.printStackTrace();
                throw new MiException("Error al procesar la imagen: " + e.getMessage());
            }
        }
        return null;
    }

    public void validar(String nombre, byte[] contenido, TipoImagen tipoImagen) throws MiException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new MiException("El nombre de la imagen no puede ser nulo o vacío");
        }
        if (contenido == null || contenido.length == 0) {
            throw new MiException("El contenido de la imagen no puede ser nulo o vacío");
        }
        if (tipoImagen == null) {
            throw new MiException("El tipo de imagen no puede ser nulo");
        }
    }

    public Imagen actualizar(MultipartFile archivo, String idImagen, TipoImagen tipoImagen) throws MiException{
        String nombre = archivo != null ? archivo.getOriginalFilename() : null;
        byte[] contenido = extraerContenido(archivo);
 
        validar(nombre, contenido, tipoImagen);
 
        Optional<Imagen> respuesta = imagenRepositorio.findById(idImagen);
        if (respuesta.isEmpty()) {
            throw new MiException("No existe una imagen con id: " + idImagen);
        }
 
        Imagen imagen = respuesta.get();
        imagen.setNombre(nombre);
        imagen.setMime(archivo.getContentType());
        imagen.setContenido(contenido);
        imagen.setTipoImagen(tipoImagen);
 
        return imagenRepositorio.save(imagen);
    }

    public Imagen findById(String id) {
        return imagenRepositorio.findById(id).orElse(null);
    }

    private byte[] extraerContenido(MultipartFile archivo) throws MiException {
        if (archivo == null) {
            return null;
        }
        try {
            return archivo.getBytes();
        } catch (Exception e) {
            throw new MiException("Error al procesar la imagen: " + e.getMessage());
        }
    }
}
