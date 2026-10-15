package com.tienda.carrito.business.logic.service;

import com.tienda.carrito.business.domain.entity.BaseEntity;
import com.tienda.carrito.business.logic.error.ErrorServiceException;
import com.tienda.carrito.business.percistence.repository.BaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * SERVICIO BASE - Template Method (misma estructura que patronTemplate).
 *
 * Metodos plantilla: alta(), modificar() y baja(). Cada uno fija el orden de los pasos:
 *
 *   alta       : validar(ALTA)         -> preAlta         -> save -> postAlta
 *   modificar  : validar(MODIFICACION) -> preModificacion -> save
 *   baja       : validar(BAJA)         -> preBaja         -> marca eliminado -> save
 *
 * Paso obligatorio de cada servicio: validar(useCase, entidad).  Hooks opcionales: pre/post.
 *
 * Reglas de arquitectura:
 *  - Las reglas de negocio se escriben SOLO en los servicios (validar + hooks).
 *  - Un servicio habla con UN solo repositorio (el suyo). Para otra entidad usa el otro SERVICIO.
 *
 * Diferencias puntuales con patronTemplate (corrigen errores del original):
 *  - Es transaccional (rollbackFor = Exception.class, porque ErrorServiceException es checked y por defecto
 *    Spring NO revierte con excepciones checked).
 *  - obtenerEntidad() ya no tapa el mensaje real con "Error de Sistemas".
 *  - modificar() fuerza eliminado=false (un formulario no puede "resucitar" ni borrar registros) y no modifica registros dados de baja.
 *  - baja() ejecuta preBaja() (en el original estaba declarado pero nunca se llamaba).
 *  - Los errores inesperados se registran en el log antes de devolver "Error de Sistemas".
 *
 * Los metodos no son "final" a proposito: Spring genera un proxy CGLIB para @Transactional.
 */
public abstract class BaseService<T extends BaseEntity<ID>, ID> {

    private static final Logger log = LoggerFactory.getLogger(BaseService.class);

    protected final BaseRepository<T, ID> repository;

    protected BaseService(BaseRepository<T, ID> repository) {
        this.repository = repository;
    }

    // ===================== Metodos plantilla =====================

    @Transactional(rollbackFor = Exception.class)
    public T alta(T entidad) throws ErrorServiceException {
        try {
            entidad.setId(null);            // un alta nunca trae id
            entidad.setEliminado(false);
            validar(BaseUseCaseService.ALTA, entidad);
            preAlta(entidad);
            T guardada = repository.save(entidad);
            postAlta(guardada);
            return guardada;
        } catch (ErrorServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado en alta", e);
            throw new ErrorServiceException("Error de Sistemas");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Optional<T> modificar(ID id, T entidadNueva) throws ErrorServiceException {
        try {
            entidadNueva.setId(id);
            entidadNueva.setEliminado(false);
            validar(BaseUseCaseService.MODIFICACION, entidadNueva);
            preModificacion(entidadNueva);
            return repository.findById(id)
                    .filter(actual -> !Boolean.TRUE.equals(actual.isEliminado()))
                    .map(actual -> repository.save(entidadNueva));
        } catch (ErrorServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado en modificar", e);
            throw new ErrorServiceException("Error de Sistemas");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void baja(ID id) throws ErrorServiceException {
        try {
            T entidadBaja = obtenerEntidad(id);
            validar(BaseUseCaseService.BAJA, entidadBaja);
            preBaja(entidadBaja);
            entidadBaja.setEliminado(true);
            repository.save(entidadBaja);
        } catch (ErrorServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado en baja", e);
            throw new ErrorServiceException("Error de Sistemas");
        }
    }

    // ===================== Consultas =====================

    @Transactional(readOnly = true)
    public Optional<T> obtener(ID id) throws ErrorServiceException {
        try {
            return repository.findById(id)
                    .filter(entidad -> !Boolean.TRUE.equals(entidad.isEliminado()));
        } catch (Exception e) {
            log.error("Error inesperado en obtener", e);
            throw new ErrorServiceException("Error de Sistemas");
        }
    }

    /** Igual que obtener(), pero lanza ErrorServiceException si no existe. */
    @Transactional(readOnly = true)
    public T obtenerEntidad(ID id) throws ErrorServiceException {
        return obtener(id).orElseThrow(() -> new ErrorServiceException("No se encontró el registro solicitado."));
    }

    @Transactional(readOnly = true)
    public List<T> listarActivos() throws ErrorServiceException {
        try {
            return repository.findAll().stream()
                    .filter(entidad -> !Boolean.TRUE.equals(entidad.isEliminado()))
                    .toList();
        } catch (Exception e) {
            log.error("Error inesperado en listarActivos", e);
            throw new ErrorServiceException("Error de Sistemas");
        }
    }

    // ===================== Pasos que define cada servicio =====================

    /** Reglas de negocio. Debe lanzar ErrorServiceException con un mensaje claro si algo no se cumple. */
    protected abstract void validar(BaseUseCaseService useCase, T entidad) throws ErrorServiceException;

    /** Hooks opcionales (vacios por defecto). */
    protected void preAlta(T entidad) throws ErrorServiceException { }

    protected void postAlta(T entidad) throws ErrorServiceException { }

    /** En modificar() la entidad recibida REEMPLAZA a la guardada: aca se copian los campos que no vienen del formulario. */
    protected void preModificacion(T entidadNueva) throws ErrorServiceException { }

    protected void preBaja(T entidad) throws ErrorServiceException { }

    // ===================== Utilidad =====================

    protected boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
