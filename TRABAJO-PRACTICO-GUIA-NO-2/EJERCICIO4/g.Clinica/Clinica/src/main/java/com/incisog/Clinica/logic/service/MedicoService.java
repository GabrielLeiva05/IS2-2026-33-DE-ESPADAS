package com.incisog.Clinica.logic.service;

import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.persistence.repository.MedicoRepository;
import com.incisog.Clinica.models.Medico;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicoService {
    private final MedicoRepository repository;

    public MedicoService(MedicoRepository repository){
        this.repository = repository;
    }

    public List<Medico> listar(){
        return repository.findByEliminadoFalse();
    }

    public Medico buscarPorId(String id) throws ErrorServiceException {
        return repository.findById(id)
                .filter(medico -> !(medico.isEliminado()))
                .orElseThrow(() ->
                        new ErrorServiceException("Médico no encontrado o eliminado.")
                );
    }

    private void validar(Medico medico) throws ErrorServiceException{

        if (medico == null) {
            throw new ErrorServiceException("Debe indicar el medico");
        }

        if (medico.getNombre() == null || medico.getNombre().trim().isEmpty()) {
            throw new ErrorServiceException("Debe indicar el nombre");
        }

        if (medico.getApellido() == null || medico.getApellido().trim().isEmpty()) {
            throw new ErrorServiceException("Debe indicar el apellido");
        }

        if (medico.getDocumento() == null || medico.getDocumento().trim().isEmpty()) {
            throw new ErrorServiceException("Debe indicar el documento");
        }

    }

    private void chequearDocumento(Medico medico) throws ErrorServiceException {
        if (repository.existsByDocumentoAndEliminadoFalse(medico.getDocumento())){
            throw new ErrorServiceException("El documento dado ya esta usado");
        }
    }

    public void crear(Medico medico) throws ErrorServiceException {
        validar(medico);
        chequearDocumento(medico);
        medico.setId(null);
        medico.setEliminado(false);
        repository.save(medico);

    }

    public void modificar(String id, Medico datos) throws ErrorServiceException {
        validar(datos);

        Medico medico = buscarPorId(id);

        if (!medico.getDocumento().equals(datos.getDocumento())) {
            chequearDocumento(datos);
        }

        medico.setNombre(datos.getNombre());
        medico.setApellido(datos.getApellido());
        medico.setDocumento(datos.getDocumento());
        repository.save(medico);
    }

    public void eliminar(String id) throws ErrorServiceException {
        Medico medico = buscarPorId(id);
        medico.setEliminado(true);
        repository.save(medico);
    }
}
