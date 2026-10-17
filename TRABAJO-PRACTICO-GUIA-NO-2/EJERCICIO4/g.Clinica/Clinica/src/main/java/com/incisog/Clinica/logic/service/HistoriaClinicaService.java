package com.incisog.Clinica.logic.service;

import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.persistence.repository.HistoriaClinicaRepository;
import com.incisog.Clinica.models.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HistoriaClinicaService extends BaseServiceTemplate<HistoriaClinica> {

    private final HistoriaClinicaRepository historiaRepository;
    private final PacienteService pacienteService;
    private final UsuarioService usuarioService;

    public HistoriaClinicaService(HistoriaClinicaRepository repository,
                                  PacienteService pacienteService,
                                  UsuarioService usuarioService) {
        super(repository);                       // 1) la base recibe solo lo suyo: el repositorio
        this.historiaRepository = repository;    // 2) y lo que necesita la hija, lo guarda la hija
        this.pacienteService = pacienteService;
        this.usuarioService = usuarioService;
    }

    public HistoriaClinica buscarPorPaciente(String pacienteId) throws ErrorServiceException {
        return historiaRepository.findByPacienteIdAndEliminadoFalse(pacienteId)
                .orElseThrow(() -> new ErrorServiceException("El paciente no tiene historia clínica."));
    }

    @Override
    protected void validar(HistoriaClinica historia) throws ErrorServiceException {
        if (historia == null || historia.getPaciente() == null || historia.getPaciente().getId() == null) {
            throw new ErrorServiceException("Debe indicar el paciente");
        }
        if (historia.getUsuario() == null || historia.getUsuario().getId() == null) {
            throw new ErrorServiceException("Debe indicar el usuario");
        }
    }

    @Override
    protected void copiarDatos(HistoriaClinica existente, HistoriaClinica datos) {
    }

    @Override
    protected void antesDeCrear(HistoriaClinica historia) throws ErrorServiceException {
        Paciente paciente = pacienteService.buscarPorId(historia.getPaciente().getId());
        Usuario usuario = usuarioService.buscarPorId(historia.getUsuario().getId());

        // Regla de negocio: una sola historia activa por paciente.
        if (historiaRepository.existsByPacienteIdAndEliminadoFalse(paciente.getId())) {
            throw new ErrorServiceException("El paciente ya tiene una historia clínica");
        }

        //Deberia de ingresar aca las referencias a los objetos correspondientes, porque si no... ¿donde?
        historia.setPaciente(paciente);
        historia.setUsuario(usuario);
        historia.setDetalles(new ArrayList<>());
    }

    @Override
    protected void antesDeEliminar(HistoriaClinica historia) throws ErrorServiceException {
        for (DetalleHistoriaClinica detalle : historia.getDetalles()) {
            detalle.setEliminado(true);
        }
    }
}
