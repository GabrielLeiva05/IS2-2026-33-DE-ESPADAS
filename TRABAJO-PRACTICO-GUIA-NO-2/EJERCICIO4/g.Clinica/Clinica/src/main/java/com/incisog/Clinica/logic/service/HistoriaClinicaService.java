package com.incisog.Clinica.logic.service;

import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.persistence.repository.HistoriaClinicaRepository;
import com.incisog.Clinica.models.DetalleHistoriaClinica;
import com.incisog.Clinica.models.HistoriaClinica;
import com.incisog.Clinica.models.Paciente;
import com.incisog.Clinica.models.Usuario;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HistoriaClinicaService {

    private final HistoriaClinicaRepository repository;
    private final PacienteService pacienteService;
    private final UsuarioService usuarioService;

    public HistoriaClinicaService(HistoriaClinicaRepository repository,
                                  PacienteService pacienteService,
                                  UsuarioService usuarioService) {
        this.repository = repository;
        this.pacienteService = pacienteService;
        this.usuarioService = usuarioService;
    }

    public List<HistoriaClinica> listar() {
        return repository.findByEliminadoFalse();
    }

    public HistoriaClinica buscarPorId(String id) throws ErrorServiceException {
        return repository.findById(id)
                .filter(historia -> !historia.isEliminado())
                .orElseThrow(() -> new ErrorServiceException("Historia clínica no encontrada o eliminada."));
    }

    public HistoriaClinica buscarPorPaciente(String pacienteId) throws ErrorServiceException {
        return repository.findByPacienteIdAndEliminadoFalse(pacienteId)
                .orElseThrow(() -> new ErrorServiceException("El paciente no tiene historia clínica."));
    }

    /**
     * Crea la historia de un paciente. Del JSON solo se usan los ids:
     * {"paciente": {"id": "..."}, "usuario": {"id": "..."}}
     * Con esos ids se buscan las entidades reales (y así se valida que existan y estén activas).
     */
    public HistoriaClinica crear(HistoriaClinica historia) throws ErrorServiceException {
        if (historia == null || historia.getPaciente() == null || historia.getPaciente().getId() == null) {
            throw new ErrorServiceException("Debe indicar el paciente");
        }
        if (historia.getUsuario() == null || historia.getUsuario().getId() == null) {
            throw new ErrorServiceException("Debe indicar el usuario");
        }

        Paciente paciente = pacienteService.buscarPorId(historia.getPaciente().getId());
        Usuario usuario = usuarioService.buscarPorId(historia.getUsuario().getId());

        // Regla de negocio: una sola historia activa por paciente.
        if (repository.existsByPacienteIdAndEliminadoFalse(paciente.getId())) {
            throw new ErrorServiceException("El paciente ya tiene una historia clínica");
        }

        HistoriaClinica nueva = new HistoriaClinica();
        nueva.setPaciente(paciente);
        nueva.setUsuario(usuario);
        nueva.setDetalles(new ArrayList<>());
        nueva.setEliminado(false);
        return repository.save(nueva);
    }

    /**
     * Baja lógica de la historia y, por composición, de todos sus detalles.
     */
    @Transactional(rollbackFor = ErrorServiceException.class)
    public void eliminar(String id) throws ErrorServiceException {
        HistoriaClinica historia = buscarPorId(id);
        historia.setEliminado(true);
        for (DetalleHistoriaClinica detalle : historia.getDetalles()) {
            detalle.setEliminado(true);
        }
        repository.save(historia);
    }
}
