package com.is2.tinder.services;

import com.is2.tinder.repositories.MascotaRepository;
import com.is2.tinder.repositories.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.is2.tinder.entities.Foto;
import com.is2.tinder.entities.Mascota;
import com.is2.tinder.entities.Usuario;
import com.is2.tinder.enumerations.Sexo;
import com.is2.tinder.errors.ErrorService;

@Service
public class MascotaService {
    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FotoService fotoService;
    
    public MascotaService(MascotaRepository mascotaRepository, UsuarioRepository usuarioRepository, FotoService fotoService) {
        this.mascotaRepository = mascotaRepository;
        this.fotoService = fotoService;
        this.usuarioRepository = usuarioRepository;
    }

    public void agregarMascota(MultipartFile archivo, String idUsuario, String nombre, Sexo sexo) throws ErrorService {
        Usuario usuario = usuarioRepository.findById(idUsuario).get();
        
        validar(nombre, sexo);

        Mascota mascota = new Mascota();
        mascota.setNombre(nombre);
        mascota.setSexo(sexo);
        mascota.setAlta(LocalDateTime.now());
        mascota.setUsuario(usuario);

        Foto foto = fotoService.guardar(archivo);
        mascota.setFoto(foto);

        mascotaRepository.save(mascota);
    }

    public void modificar(MultipartFile archivo, String idUsuario, String idMascota, String nombre, Sexo sexo) throws ErrorService {
        validar(nombre, sexo);

        Optional<Mascota> respuesta = mascotaRepository.findById(idMascota);

        if (respuesta.isPresent()) {
            Mascota mascota = respuesta.get();
            if (mascota.getUsuario().getId().equals(idUsuario)) {
                mascota.setNombre(nombre);
                mascota.setSexo(sexo);

                String idFoto = null;
                if (mascota.getFoto() != null) {
                    idFoto = mascota.getFoto().getId();
                }
                Foto foto = fotoService.actualizar(idFoto, archivo);
                mascota.setFoto(foto);
                
                mascotaRepository.save(mascota);
            } else {
                throw new ErrorService("No tiene permisos suficientes para realizar la operación.");
            }
        } else {
            throw new ErrorService("No existe una mascota con el identificador solicitado.");
        }
    }

    public void eliminar(String idUsuario, String idMascota) throws ErrorService {
        Optional<Mascota> respuesta = mascotaRepository.findById(idMascota);
        if(respuesta.isPresent()) {
            Mascota mascota = respuesta.get();
            if (mascota.getUsuario().getId().equals(idUsuario)) {
                mascota.setBaja(LocalDateTime.now());
                mascotaRepository.save(mascota);
            } else {
                throw new ErrorService("No tiene permisos suficientes para realizar la operación.");
            }
        } else {
            throw new ErrorService("No existe una mascota con el identificador solicitado.");
        }
    }
    private void validar(String nombre, Sexo sexo) throws ErrorService {
        if (nombre == null || nombre.isBlank()) {
            throw new ErrorService("El nombre de la mascota es obligatorio");
        }
        if (sexo == null) {
            throw new ErrorService("El sexo de la mascota es obligatorio");
        }
    }
}
