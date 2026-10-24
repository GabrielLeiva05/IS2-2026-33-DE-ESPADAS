package com.incisog.Clinica.logic.service;

import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.persistence.repository.MedicoRepository;
import com.incisog.Clinica.models.Medico;
import org.springframework.stereotype.Service;


@Service
public class MedicoService extends BaseServiceTemplate<Medico> {

    private final MedicoRepository medicoRepository;

    public MedicoService(MedicoRepository repository){
        super(repository);
        this.medicoRepository = repository; // vos lo guardás como MedicoRepository
    }

    @Override
    protected void validar(Medico medico) throws ErrorServiceException{

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
        if (medicoRepository.existsByDocumentoAndEliminadoFalse(medico.getDocumento())){
            throw new ErrorServiceException("El documento dado ya esta usado");
        }
    }

    @Override
    protected void antesDeCrear(Medico medico) throws ErrorServiceException {
        chequearDocumento(medico);
    }



    @Override
    protected void copiarDatos(Medico existente, Medico datos) {
        existente.setNombre(datos.getNombre());
        existente.setApellido(datos.getApellido());
        existente.setDocumento(datos.getDocumento());

    }

    @Override
    public void antesDeModificar(Medico existente, Medico datos) throws ErrorServiceException{
        //Se revisa que el documento nuevo no exista en la BD
        if (!existente.getDocumento().equals(datos.getDocumento())) {
            chequearDocumento(datos);
        }
    }
}
