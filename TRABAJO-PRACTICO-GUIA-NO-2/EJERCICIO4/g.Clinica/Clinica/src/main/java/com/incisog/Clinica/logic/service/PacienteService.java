package com.incisog.Clinica.logic.service;

import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.persistence.repository.PacienteRepository;
import com.incisog.Clinica.models.FotoPaciente;
import com.incisog.Clinica.models.Paciente;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PacienteService {
    private final PacienteRepository repository;

    public PacienteService(PacienteRepository repository) {
        this.repository = repository;
    }

    public List<Paciente> listar() {
        return repository.findByEliminadoFalse();
    }

    public Paciente buscarPorId(String id) throws ErrorServiceException {
        return repository.findById(id)
                .filter(paciente -> !paciente.isEliminado())
                .orElseThrow(() -> new ErrorServiceException("Paciente no encontrado o eliminado."));
    }

    public Paciente crear(Paciente paciente) throws ErrorServiceException {
        validar(paciente);
        chequearDocumento(paciente.getDocumento());
        paciente.setId(null);
        paciente.setEliminado(false);
        paciente.setFoto(null); // la foto se sube por su propio endpoint
        return repository.save(paciente);
    }

    public Paciente modificar(String id, Paciente datos) throws ErrorServiceException {
        validar(datos);
        Paciente paciente = buscarPorId(id);

        // Si se corrige el documento (error de carga), el nuevo no puede ser de otro paciente.
        if (!paciente.getDocumento().equals(datos.getDocumento())) {
            chequearDocumento(datos.getDocumento());
        }

        paciente.setNombre(datos.getNombre());
        paciente.setApellido(datos.getApellido());
        paciente.setDocumento(datos.getDocumento());
        return repository.save(paciente);
    }

    public void eliminar(String id) throws ErrorServiceException {
        Paciente paciente = buscarPorId(id);
        paciente.setEliminado(true);
        repository.save(paciente);
    }

    // Lo usa FotoPacienteService después de guardar la foto.
    public Paciente asignarFoto(Paciente paciente, FotoPaciente foto) {
        paciente.setFoto(foto);
        return repository.save(paciente);
    }

    private void validar(Paciente paciente) throws ErrorServiceException {
        if (paciente == null) {
            throw new ErrorServiceException("Debe indicar el paciente");
        }
        if (paciente.getNombre() == null || paciente.getNombre().trim().isEmpty()) {
            throw new ErrorServiceException("Debe indicar el nombre");
        }
        if (paciente.getApellido() == null || paciente.getApellido().trim().isEmpty()) {
            throw new ErrorServiceException("Debe indicar el apellido");
        }
        if (paciente.getDocumento() == null || paciente.getDocumento().trim().isEmpty()) {
            throw new ErrorServiceException("Debe indicar el documento");
        }
    }

    private void chequearDocumento(String documento) throws ErrorServiceException {
        if (repository.existsByDocumentoAndEliminadoFalse(documento)) {
            throw new ErrorServiceException("Ya existe un paciente con ese documento");
        }
    }

    // Jackson lo agrega al JSON como "tieneFoto": le sirve al frontend para saber
    // si tiene sentido pedir la foto. No es una columna (JPA usa los atributos, no los getters).
    public boolean isTieneFoto(String id) throws ErrorServiceException {
        Paciente paciente = buscarPorId(id);
        return (paciente.getFoto() != null);
    }
}
