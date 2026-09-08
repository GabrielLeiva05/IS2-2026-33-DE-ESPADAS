package com.is2.tinder.services;

import com.is2.tinder.repositories.VotoRepository;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.is2.tinder.entities.Mascota;
import com.is2.tinder.entities.Voto;
import com.is2.tinder.dtos.ReporteVotosDTO;
import com.is2.tinder.errors.ErrorService;
import com.is2.tinder.repositories.MascotaRepository;

@Service
public class VotoService {
    private final VotoRepository votoRepository;
    private final MascotaRepository mascotaRepository;
    private final NotificacionService notificacionService;

    public VotoService(MascotaRepository mascotaRepository, VotoRepository votoRepository, NotificacionService notificacionService) {
        this.mascotaRepository = mascotaRepository;
        this.votoRepository = votoRepository;
        this.notificacionService = notificacionService;
    }

    public void votar(String idUsuario, String idMascota1, String idMascota2) throws ErrorService {
        Voto voto = new Voto();
        voto.setFecha(LocalDateTime.now());

        if (idMascota1.equals(idMascota2) ) {
            throw new ErrorService("No puede votarse a si mismo.");
        }

        Optional<Mascota> respuesta = mascotaRepository.findById(idMascota1);
        if (respuesta.isPresent()) {
            Mascota mascota1 = respuesta.get();
            if (mascota1.getUsuario().getId().equals(idUsuario)) {
                voto.setMascota1(mascota1);
            } else {
                throw new ErrorService("No tiene permisos para realizar la operacion solicitada.");
            }
        } else {
            throw new ErrorService("No existe una mascota vinculada a ese identificador.");
        }

        Optional<Mascota> respuesta2 = mascotaRepository.findById(idMascota2);
        if (respuesta2.isPresent()) {
            Mascota mascota2 = respuesta2.get();
            voto.setMascota2(mascota2);

            notificacionService.enviar("Tu mascota ha sido votada", "Tinder", mascota2.getUsuario().getMail());
        } else {
            throw new ErrorService("No existe una mascota vinculada a ese identificador");
        }

        votoRepository.save(voto);
    }

    public java.util.List<ReporteVotosDTO> listarReporteVotos() {
        return votoRepository.reporteVotos();
    }

    public void responder(String idUsuario, String idVoto) throws ErrorService {
        Optional<Voto> respuesta = votoRepository.findById(idVoto);
        if (respuesta.isPresent()) {
            Voto voto = respuesta.get();
            voto.setRespuesta(LocalDateTime.now());
            if (voto.getMascota2().getUsuario().getId().equals(idUsuario)) {
                notificacionService.enviar("Tu voto fue correspondido", "Tinder", voto.getMascota1().getUsuario().getMail());
                votoRepository.save(voto);
            } else {
                throw new ErrorService("No tiene permiso para realizar la operación.");
            }
        } else {
            throw new ErrorService("No existe el voto solicitado");
        }
    }
}
