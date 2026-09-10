package com.ejercicioIntegrador.tiendaderopa.service;

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
    public Imagen guardar(MultipartFile archivo) throws MiException {
        if (archivo != null && !archivo.isEmpty()) {
            try {
                Imagen imagen = new Imagen();
                imagen.setMime(archivo.getContentType());
                imagen.setNombre(archivo.getOriginalFilename());
                imagen.setContenido(archivo.getBytes());
                return imagenRepositorio.save(imagen);
            } catch (Exception e) {
                e.printStackTrace();
                throw new MiException("Error al procesar la imagen: " + e.getMessage());
            }
        }
        return null;
    }

    public Imagen actualizar(MultipartFile archivo, Long idImagen) throws MiException{
        if(archivo !=null){
            try{
                Imagen imagen = new Imagen();
                if (idImagen !=null){
                    Optional<Imagen> respuesta = imagenRepositorio.findById(idImagen);

                    if(respuesta.isPresent()){
                        imagen= respuesta.get();
                    }
                }

                imagen.setMime(archivo.getContentType());
                imagen.setNombre(archivo.getName());
                imagen.setContenido(archivo.getBytes());
                return imagenRepositorio.save(imagen);
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
        return null;
    }

    public Imagen findById(Long id) throws Exception {
        try {
            Optional<Imagen> opt = imagenRepositorio.findById(id);
            return opt.get();
        } catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }
}
