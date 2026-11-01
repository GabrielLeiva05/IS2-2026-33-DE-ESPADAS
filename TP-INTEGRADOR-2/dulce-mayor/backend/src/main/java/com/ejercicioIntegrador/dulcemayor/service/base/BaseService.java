package com.ejercicioIntegrador.dulcemayor.service.base;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * Patron Template Method para la capa de servicio.
 *
 * Los metodos alta, modificar, baja, consultar, listar y listarActivos definen el ALGORITMO
 * (el orden de los pasos y la transaccion) y es igual para todas las entidades del sistema.
 * Las subclases solo completan los PASOS:
 *  - Pasos obligatorios (abstractos): crearEntidad, actualizarEntidad, validar,
 *    convertirAResponse y nombreEntidad.
 *  - Ganchos opcionales (hooks): preAlta, postAlta, preModificacion, postModificacion,
 *    preBaja y postBaja. Por defecto no hacen nada y se redefinen solo cuando una entidad
 *    necesita acciones antes o despues de guardar.
 *
 * El intercambio con los controladores se hace siempre con DTO: REQ es el DTO de entrada
 * y RES el DTO de salida. Las entidades solo circulan entre servicios.
 *
 * Los metodos plantilla NO se declaran final porque Spring crea proxies por subclase
 * (CGLIB) para aplicar @Transactional, y un metodo final no puede ser interceptado.
 *
 * @param <E>   entidad del modelo
 * @param <REQ> DTO de entrada (alta y modificacion)
 * @param <RES> DTO de salida
 */
public abstract class BaseService<E extends BaseEntity, REQ, RES extends BaseResponseDTO> {

    protected final BaseRepository<E> repositorio;

    protected BaseService(BaseRepository<E> repositorio) {
        this.repositorio = repositorio;
    }

    // ------------------------------------------------------------------
    // Metodos plantilla
    // ------------------------------------------------------------------

    @Transactional(rollbackFor = Exception.class)
    public RES alta(REQ dto) throws MiException {
        verificarDatosRecibidos(dto);
        E entidad = crearEntidad(dto);
        entidad.setEliminado(false);
        validar(BaseUseCaseService.ALTA, entidad);
        preAlta(entidad);
        E guardada = repositorio.save(entidad);
        postAlta(guardada);
        return convertirAResponse(guardada);
    }

    @Transactional(rollbackFor = Exception.class)
    public RES modificar(String id, REQ dto) throws MiException {
        verificarDatosRecibidos(dto);
        E entidad = obtenerEntidad(id);
        actualizarEntidad(entidad, dto);
        validar(BaseUseCaseService.MODIFICACION, entidad);
        preModificacion(entidad);
        E guardada = repositorio.save(entidad);
        postModificacion(guardada);
        return convertirAResponse(guardada);
    }

    @Transactional(rollbackFor = Exception.class)
    public void baja(String id) throws MiException {
        E entidad = obtenerEntidad(id);
        validar(BaseUseCaseService.BAJA, entidad);
        preBaja(entidad);
        entidad.setEliminado(true);
        repositorio.save(entidad);
        postBaja(entidad);
    }

    @Transactional(readOnly = true)
    public RES consultar(String id) throws MiException {
        return convertirAResponse(obtenerEntidad(id));
    }

    /** Todos los registros, incluidos los dados de baja (listarX() del diagrama). */
    @Transactional(readOnly = true)
    public List<RES> listar() {
        return convertirAResponse(repositorio.findAll());
    }

    /** Solo los registros activos (listarXActivo() del diagrama). */
    @Transactional(readOnly = true)
    public List<RES> listarActivos() {
        return convertirAResponse(repositorio.findByEliminadoFalse());
    }

    // ------------------------------------------------------------------
    // Operaciones comunes para la comunicacion entre servicios
    // ------------------------------------------------------------------

    /**
     * Devuelve la entidad activa con el id indicado. La usan otros servicios cuando
     * necesitan relacionarse con esta entidad, sin acceder a su repositorio.
     */
    @Transactional(readOnly = true)
    public E obtenerEntidad(String id) throws MiException {
        if (id == null || id.isBlank()) {
            throw new MiException("Debe indicar el identificador de " + nombreEntidad());
        }
        return repositorio.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe " + nombreEntidad() + " con id " + id));
    }

    @Transactional(readOnly = true)
    public List<E> listarEntidadesActivas() {
        return repositorio.findByEliminadoFalse();
    }

    /**
     * Resuelve una entidad relacionada a traves de SU servicio. Si no existe, el error se informa
     * como dato invalido (400) y no como recurso inexistente (404), porque el id llego dentro del DTO.
     */
    protected <R extends BaseEntity> R obtenerReferencia(BaseService<R, ?, ?> servicio, String id,
                                                        String descripcion) throws MiException {
        if (id == null || id.isBlank()) {
            throw new MiException("Debe indicar " + descripcion);
        }
        try {
            return servicio.obtenerEntidad(id);
        } catch (RecursoNoEncontradoException e) {
            throw new MiException("No se encontró " + descripcion + " con id " + id
                    + ". Verifique que exista y que no se haya dado de baja.");
        }
    }

    /** Recorta los espacios de un texto recibido; devuelve null si no se informó. */
    protected static String normalizarTexto(String valor) {
        return valor == null ? null : valor.trim();
    }

    protected static boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    protected List<RES> convertirAResponse(Collection<E> entidades) {
        return entidades.stream().map(this::convertirAResponse).toList();
    }

    private void verificarDatosRecibidos(REQ dto) throws MiException {
        if (dto == null) {
            throw new MiException("Debe indicar los datos de " + nombreEntidad());
        }
    }

    // ------------------------------------------------------------------
    // Pasos obligatorios: los implementa cada servicio concreto
    // ------------------------------------------------------------------

    /** Construye una entidad nueva a partir del DTO, resolviendo sus relaciones. */
    protected abstract E crearEntidad(REQ dto) throws MiException;

    /** Copia en la entidad existente los datos modificables del DTO. */
    protected abstract void actualizarEntidad(E entidad, REQ dto) throws MiException;

    /** Reglas de negocio de la entidad para el caso de uso indicado. */
    protected abstract void validar(BaseUseCaseService casoDeUso, E entidad) throws MiException;

    /** Convierte la entidad en su DTO de salida. */
    public abstract RES convertirAResponse(E entidad);

    /** Nombre de la entidad para los mensajes, con articulo: "la categoria", "el producto". */
    protected abstract String nombreEntidad();

    // ------------------------------------------------------------------
    // Ganchos opcionales (hooks): por defecto no realizan ninguna accion
    // ------------------------------------------------------------------

    protected void preAlta(E entidad) throws MiException {
    }

    protected void postAlta(E entidad) throws MiException {
    }

    protected void preModificacion(E entidad) throws MiException {
    }

    protected void postModificacion(E entidad) throws MiException {
    }

    protected void preBaja(E entidad) throws MiException {
    }

    protected void postBaja(E entidad) throws MiException {
    }
}
