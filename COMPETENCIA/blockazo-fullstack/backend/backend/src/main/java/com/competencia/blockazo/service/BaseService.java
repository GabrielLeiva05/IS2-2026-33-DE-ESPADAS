package com.competencia.blockazo.service;

import com.competencia.blockazo.error.EntidadNoEncontradaException;
import com.competencia.blockazo.error.ErrorServiceException;
import com.competencia.blockazo.model.entity.BaseEntity;
import com.competencia.blockazo.repository.BaseRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * PATRON TEMPLATE METHOD (basado en patronTemplate).
 *
 * Los metodos alta / modificar / baja son los "metodos plantilla": definen el
 * algoritmo FIJO de cada caso de uso del ABM. Los pasos variables
 * (validar, preAlta, postAlta, preModificacion, postModificacion, preBaja, postBaja)
 * son "hooks" que cada servicio concreto (ej. UsuarioService) redefine.
 *
 *   alta:       setEliminado(false) -> validar(ALTA) -> preAlta -> save -> postAlta
 *   modificar:  existe? -> validar(MODIFICACION) -> preModificacion -> save -> postModificacion
 *   baja:       existe? -> validar(BAJA) -> preBaja -> eliminado=true -> save -> postBaja
 */
public abstract class BaseService<T extends BaseEntity<ID>, ID> {

    protected final BaseRepository<T, ID> repository;

    protected BaseService(BaseRepository<T, ID> repository) {
        this.repository = repository;
    }

    // ========================= METODOS PLANTILLA =========================

    @Transactional(rollbackFor = Exception.class)
    public T alta(T entidad) throws ErrorServiceException {
        try {
            entidad.setId(null);            // un alta nunca pisa un registro existente
            entidad.setEliminado(false);
            validar(BaseUseCaseService.ALTA, entidad);
            preAlta(entidad);

            T guardado = repository.save(entidad);

            postAlta(guardado);
            return guardado;

        } catch (ErrorServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ErrorServiceException("Error de Sistemas");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public T modificar(ID id, T entidadNueva) throws ErrorServiceException {
        try {
            T actual = obtenerEntidad(id);   // si no existe -> EntidadNoEncontradaException
            entidadNueva.setId(id);
            entidadNueva.setEliminado(actual.isEliminado());

            validar(BaseUseCaseService.MODIFICACION, entidadNueva);
            preModificacion(actual, entidadNueva);

            T actualizado = repository.save(entidadNueva);

            postModificacion(actualizado);
            return actualizado;

        } catch (ErrorServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ErrorServiceException("Error de Sistemas");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void baja(ID id) throws ErrorServiceException {
        try {
            T entidadBaja = obtenerEntidad(id);
            validar(BaseUseCaseService.BAJA, entidadBaja);
            preBaja(entidadBaja);

            entidadBaja.setEliminado(true);   // baja LOGICA
            repository.save(entidadBaja);

            postBaja(entidadBaja);

        } catch (ErrorServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ErrorServiceException("Error de Sistemas");
        }
    }

    // ========================= CONSULTAS =========================

    @Transactional(readOnly = true)
    public Optional<T> obtener(ID id) throws ErrorServiceException {
        try {
            return repository.findById(id)
                    .filter(e -> !Boolean.TRUE.equals(e.isEliminado()));
        } catch (Exception e) {
            throw new ErrorServiceException("Error de Sistemas");
        }
    }

    @Transactional(readOnly = true)
    public T obtenerEntidad(ID id) throws ErrorServiceException {
        if (id == null) {
            throw new ErrorServiceException("Debe indicar el identificador");
        }
        return obtener(id).orElseThrow(
                () -> new EntidadNoEncontradaException("Entidad no encontrada o eliminada."));
    }

    @Transactional(readOnly = true)
    public List<T> listarActivos() throws ErrorServiceException {
        try {
            return repository.findAll().stream()
                    .filter(e -> !Boolean.TRUE.equals(e.isEliminado()))
                    .toList();
        } catch (Exception e) {
            throw new ErrorServiceException("Error de Sistemas");
        }
    }

    // ============================== HOOKS ==============================
    // Metodos para ser redefinidos en las clases de servicio que heredan, con el
    // objetivo de realizar acciones previas o posteriores en Altas, Bajas y Modificaciones.

    protected void validar(BaseUseCaseService useCase, T entidad) throws ErrorServiceException {}
    protected void preAlta(T entidad) throws ErrorServiceException {}
    protected void postAlta(T entidad) throws ErrorServiceException {}
    protected void preModificacion(T actual, T entidadNueva) throws ErrorServiceException {}
    protected void postModificacion(T entidad) throws ErrorServiceException {}
    protected void preBaja(T entidad) throws ErrorServiceException {}
    protected void postBaja(T entidad) throws ErrorServiceException {}
}
