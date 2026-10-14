package com.incisog.Clinica.logic.service;

import com.incisog.Clinica.logic.dto.adapter.BaseAdapter;
import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.persistence.repository.BaseRepositoryTemplate;
import com.incisog.Clinica.models.BaseEntity;

import java.util.List;
import java.util.Optional;

public abstract class BaseServiceTemplate<T extends BaseEntity<ID>, D, ID> {

    protected final BaseRepositoryTemplate<T, ID> repository;
    protected final BaseAdapter<T, D> adaptador;

    protected BaseServiceTemplate(BaseRepositoryTemplate<T, ID> repository, BaseAdapter<T, D> adaptador) {
        this.repository = repository;
        this.adaptador = adaptador;
    }

    public D alta(D dto) throws ErrorServiceException {
        try {

            T entidad = adaptador.toEntity(dto);
            entidad.setEliminado(false);

            validar(BaseUseCaseService.ALTA, entidad);
            preAlta(entidad);
            T guardado = repository.save(entidad);
            postAlta(guardado);

            return adaptador.toDTO(guardado);

        } catch (Exception e) {
            throw new ErrorServiceException("Error de Sistemas al dar de alta: " + e.getMessage());
        }
    }

    public Optional<D> modificar(ID id, D dto) throws ErrorServiceException {
        try {

            T entidadNueva = adaptador.toEntity(dto);
            entidadNueva.setId(id);

            validar(BaseUseCaseService.MODIFICACION, entidadNueva);
            preModificacion(entidadNueva);

            return repository.findById(id)
                    .map(entidadExistente -> {
                        entidadNueva.setId(id);
                        entidadNueva.setEliminado(entidadExistente.isEliminado());
                        T actualizado = repository.save(entidadNueva);
                        return adaptador.toDTO(actualizado);
                    });

        } catch (Exception e) {
            throw new ErrorServiceException("Error de Sistemas al modificar: " + e.getMessage());
        }
    }

    public void baja(ID id) throws ErrorServiceException {
        try {

            T entidad = obtenerEntidad(id);
            preBaja(entidad);
            validar(BaseUseCaseService.BAJA, entidad);

            entidad.setEliminado(true);
            repository.save(entidad);

        } catch (ErrorServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ErrorServiceException("Error de Sistemas al dar de baja: " + e.getMessage());
        }
    }

    public Optional<D> obtener(ID id) throws ErrorServiceException {
        try {
            return repository.findById(id)
                    .filter(e -> !Boolean.TRUE.equals(e.isEliminado())) // Filtra los eliminados
                    .map(adaptador::toDTO);
        } catch (Exception e) {
            throw new ErrorServiceException("Error de Sistemas al obtener: " + e.getMessage());
        }
    }

    public List<D> listarActivos() throws ErrorServiceException {
        try {

            return adaptador.listDTO(
                    repository.findAll().stream()
                            .filter(e -> !Boolean.TRUE.equals(e.isEliminado()))
                            .toList()
            );

        } catch (Exception e) {
            throw new ErrorServiceException("Error de Sistemas al listar activos: " + e.getMessage());
        }
    }

    protected T obtenerEntidad(ID id) throws ErrorServiceException {
        return repository.findById(id)
                .filter(e -> !Boolean.TRUE.equals(e.isEliminado()))
                .orElseThrow(() -> new ErrorServiceException("Entidad no encontrada o eliminada."));
    }

    protected void validar(BaseUseCaseService useCase, T entidad) throws ErrorServiceException {}
    protected void preAlta(T entidad) throws ErrorServiceException {}
    protected void postAlta(T entidad) throws ErrorServiceException {}
    protected void preModificacion(T entidad) throws ErrorServiceException {}
    protected void preBaja(T entidad) throws ErrorServiceException {}
}