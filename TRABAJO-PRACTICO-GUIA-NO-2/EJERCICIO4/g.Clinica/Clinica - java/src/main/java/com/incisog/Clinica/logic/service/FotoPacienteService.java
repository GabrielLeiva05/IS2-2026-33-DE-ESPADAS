package com.incisog.Clinica.logic.service;


import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.persistence.repository.FotoPacienteRepository;
import com.incisog.Clinica.models.FotoPaciente;
import com.incisog.Clinica.models.Paciente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FotoPacienteService {

    private final FotoPacienteRepository repository;
    private final PacienteService pacienteService;

    public FotoPacienteService(FotoPacienteRepository repository, PacienteService pacienteService) {
        this.repository = repository;
        this.pacienteService = pacienteService;
    }

    /**
     * Sube (o reemplaza) la foto de un paciente.
     * @Transactional: guardar la foto y asignarla al paciente son dos escrituras;
     * si la segunda falla, se deshace la primera. rollbackFor hace falta porque
     * ErrorServiceException es checked y, por defecto, Spring solo deshace ante unchecked.
     */
    @Transactional(rollbackFor = ErrorServiceException.class)
    public void subir(String pacienteId, MultipartFile archivo) throws ErrorServiceException {
        Paciente paciente = pacienteService.buscarPorId(pacienteId);

        if (archivo == null || archivo.isEmpty()) {
            throw new ErrorServiceException("Debe adjuntar un archivo");
        }
        String mime = archivo.getContentType();
        if (mime == null || !mime.startsWith("image/")) {
            throw new ErrorServiceException("El archivo debe ser una imagen");
        }

        // Si ya tenía foto, la reutilizamos (reemplazo); si no, creamos una nueva.
        FotoPaciente foto = paciente.getFoto() != null ? paciente.getFoto() : new FotoPaciente();
        foto.setNombre(archivo.getOriginalFilename());
        foto.setMime(mime);
        try {
            foto.setContenido(archivo.getBytes());
        } catch (IOException e) {
            throw new ErrorServiceException("No se pudo leer el archivo");
        }
        foto.setEliminado(false);

        FotoPaciente guardada = repository.save(foto);
        pacienteService.asignarFoto(paciente, guardada);
    }

    public FotoPaciente buscarPorPaciente(String pacienteId) throws ErrorServiceException {
        Paciente paciente = pacienteService.buscarPorId(pacienteId);
        FotoPaciente foto = paciente.getFoto();
        if (foto == null || foto.isEliminado()) {
            throw new ErrorServiceException("El paciente no tiene foto");
        }
        return foto;
    }
}
