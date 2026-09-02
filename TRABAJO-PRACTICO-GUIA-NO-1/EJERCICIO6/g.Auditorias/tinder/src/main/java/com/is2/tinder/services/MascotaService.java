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
import com.is2.tinder.dtos.MascotaDTO;
import com.is2.tinder.enumerations.Sexo;
import com.is2.tinder.enumerations.Tipo;
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

    public void agregarMascota(String idUsuario, String nombre, Sexo sexo, MultipartFile archivo, Tipo tipo) throws ErrorService {
        agregarMascota(archivo, idUsuario, nombre, sexo);
        Mascota mascota = mascotaRepository.buscarMascotasPorUsuario(idUsuario).stream()
                .filter(item -> nombre.equals(item.getNombre()))
                .findFirst()
                .orElseThrow(() -> new ErrorService("No se pudo guardar la mascota"));
        mascota.setTipo(tipo);
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

    public void modificar(String idUsuario, String idMascota, String nombre, Sexo sexo, MultipartFile archivo, Tipo tipo) throws ErrorService {
        modificar(archivo, idUsuario, idMascota, nombre, sexo);
        Mascota mascota = mascotaRepository.findById(idMascota)
                .orElseThrow(() -> new ErrorService("No existe una mascota con el identificador solicitado."));
        mascota.setTipo(tipo);
        mascotaRepository.save(mascota);
    }

    public MascotaDTO buscarMascota(String idMascota) throws ErrorService {
        if (idMascota == null || idMascota.isBlank()) {
            throw new ErrorService("Debe indicar una mascota");
        }
        return MascotaDTO.fromEntity(mascotaRepository.findById(idMascota).orElse(null));
    }

    public java.util.List<MascotaDTO> listarMascotaPorUsuario(String idUsuario) throws ErrorService {
        return mascotaRepository.listarMascotasPorUsuario(idUsuario).stream().map(MascotaDTO::fromEntity).toList();
    }

    public java.util.List<MascotaDTO> listarMascotaDeBaja(String idUsuario) throws ErrorService {
        return mascotaRepository.listarMascotasDeBaja(idUsuario).stream().map(MascotaDTO::fromEntity).toList();
    }

    public java.util.List<MascotaDTO> listarMascotasPorTipo(String idUsuario, Tipo tipo) throws ErrorService {
        java.util.List<Mascota> mascotas = tipo == null ? mascotaRepository.listarMascotasMenosUsuario(idUsuario)
                : mascotaRepository.listarMascotasMenosUsuarioPorTipo(idUsuario, tipo);
        return mascotas.stream().map(MascotaDTO::fromEntity).toList();
    }

    public void darAlta(String idUsuario, String idMascota) throws ErrorService {
        Mascota mascota = mascotaRepository.findById(idMascota)
                .orElseThrow(() -> new ErrorService("No existe una mascota con el identificador solicitado."));
        if (!mascota.getUsuario().getId().equals(idUsuario)) {
            throw new ErrorService("No tiene permisos suficientes para realizar la operación.");
        }
        mascota.setBaja(null);
        mascotaRepository.save(mascota);
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
