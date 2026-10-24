package com.incisog.Clinica.logic.service;

import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.persistence.repository.PacienteRepository;
import com.incisog.Clinica.models.FotoPaciente;
import com.incisog.Clinica.models.Paciente;
import org.springframework.stereotype.Service;

/**
 * Hereda de la plantilla: listar, buscarPorId, crear, modificar, eliminar.
 * Completa los pasos: validar, copiarDatos (obligatorios) y antesDeCrear, antesDeModificar (hooks).
 */
@Service
public class PacienteService extends BaseServiceTemplate<Paciente> {

    // Mismo objeto que "repository" de la base, pero con su tipo concreto
    // para poder usar existsByDocumentoAndEliminadoFalse.
    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository repository) {
        super(repository);
        this.pacienteRepository = repository;
    }

    // ---------- pasos de la plantilla ----------

    @Override
    protected void validar(Paciente paciente) throws ErrorServiceException {
        if (paciente == null) {
            throw new ErrorServiceException("Debe indicar el paciente");
        }
        if (esVacio(paciente.getNombre())) {
            throw new ErrorServiceException("Debe indicar el nombre");
        }
        if (esVacio(paciente.getApellido())) {
            throw new ErrorServiceException("Debe indicar el apellido");
        }
        if (esVacio(paciente.getDocumento())) {
            throw new ErrorServiceException("Debe indicar el documento");
        }
    }

    @Override
    protected void antesDeCrear(Paciente paciente) throws ErrorServiceException {
        chequearDocumento(paciente.getDocumento());
        paciente.setFoto(null); // la foto se sube por su propio endpoint
    }

    @Override
    protected void antesDeModificar(Paciente existente, Paciente datos) throws ErrorServiceException {
        // Solo si se corrige el documento hay que verificar que el nuevo no sea de otro paciente.
        if (!existente.getDocumento().equals(datos.getDocumento())) {
            chequearDocumento(datos.getDocumento());
        }
    }

    @Override
    protected void copiarDatos(Paciente existente, Paciente datos) {
        existente.setNombre(datos.getNombre());
        existente.setApellido(datos.getApellido());
        existente.setDocumento(datos.getDocumento());
        // la foto NO se copia: se cambia solo desde FotoPacienteService
    }

    // ---------- métodos propios de Paciente (fuera de la plantilla) ----------

    // Lo usa FotoPacienteService después de guardar la foto.
    public Paciente asignarFoto(Paciente paciente, FotoPaciente foto) {
        paciente.setFoto(foto);
        return repository.save(paciente);
    }

    // ---------- auxiliares ----------

    private void chequearDocumento(String documento) throws ErrorServiceException {
        if (pacienteRepository.existsByDocumentoAndEliminadoFalse(documento)) {
            throw new ErrorServiceException("Ya existe un paciente con ese documento");
        }
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
