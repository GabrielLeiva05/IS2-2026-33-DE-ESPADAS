package com.is2.tinder.services;

import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.is2.tinder.entities.Foto;
import com.is2.tinder.errors.ErrorService;
import com.is2.tinder.repositories.FotoRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class FotoService {
    private final FotoRepository fotoRepository;

    public FotoService(FotoRepository fotoRepository) {
        this.fotoRepository = fotoRepository;
    }

    @Transactional
    public Foto guardar(MultipartFile archivo) throws ErrorService {
        if (archivo != null) {
            try {
                Foto foto = new Foto();
                foto.setMime(archivo.getContentType());
                foto.setNombre(archivo.getName());
                foto.setContenido(archivo.getBytes());

                return fotoRepository.save(foto);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        return null;
    }

    @Transactional
    public Foto actualizar(String idFoto, MultipartFile archivo) throws ErrorService {
        if (archivo != null) {
            try {
                Foto foto = new Foto();

                if (idFoto != null) {
                    Optional<Foto> respuesta = fotoRepository.findById(idFoto);
                    if (respuesta.isPresent()) {
                        foto = respuesta.get();
                    }
                }

                foto.setMime(archivo.getContentType());
                foto.setNombre(archivo.getName());
                foto.setContenido(archivo.getBytes());

                return fotoRepository.save(foto);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        return null;
    }
}
