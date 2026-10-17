package com.incisog.Clinica.logic.service;

import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.persistence.repository.BaseRepositoryTemplate;
import com.incisog.Clinica.models.BaseEntity;

import java.util.List;


public abstract class BaseServiceTemplate<T extends BaseEntity> {

    protected final BaseRepositoryTemplate<T> repository;

    protected BaseServiceTemplate(BaseRepositoryTemplate<T> repository) {
        this.repository = repository;
    }

    public List<T> listar() {
        return repository.findByEliminadoFalse();
    }

    public T buscarPorId(String id) throws ErrorServiceException {
        return repository.findById(id)
                .filter(entidad -> !entidad.isEliminado())
                .orElseThrow(() -> new ErrorServiceException("no encontrado o eliminado."));
    }



    public T crear(T entidad) throws ErrorServiceException{
        validar(entidad);
        antesDeCrear(entidad);
        entidad.setId(null);
        entidad.setEliminado(false);
        return repository.save(entidad);
    }

    public T modificar(String id, T datos) throws ErrorServiceException{
        validar(datos);
        T existente = buscarPorId(id);
        antesDeModificar(existente, datos);
        copiarDatos(existente, datos);
        return repository.save(existente);
    }

    public void eliminar(String id) throws ErrorServiceException{
        T entidad = buscarPorId(id);
        antesDeEliminar(entidad);
        entidad.setEliminado(true);
        repository.save(entidad);
    }
    // pasos obligatorios
    protected abstract void validar(T entidad) throws ErrorServiceException;
    protected abstract void copiarDatos(T existente, T datos);

    // hooks: pasos opcionales, vacíos por defecto
    protected void antesDeCrear(T entidad) throws ErrorServiceException { }
    protected void antesDeModificar(T existente, T datos) throws ErrorServiceException { }
    //protected void modificacion(T existente, T datos) throws ErrorServiceException{ }
    protected void antesDeEliminar(T entidad) throws ErrorServiceException { }
}