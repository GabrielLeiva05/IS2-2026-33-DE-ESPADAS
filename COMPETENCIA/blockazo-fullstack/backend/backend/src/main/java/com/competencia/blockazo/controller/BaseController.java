package com.competencia.blockazo.controller;

import com.competencia.blockazo.error.ErrorServiceException;
import com.competencia.blockazo.model.entity.BaseEntity;
import com.competencia.blockazo.service.BaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Controller REST generico del patron Template (version REST del BaseController de patronTemplate).
 *
 * Define el algoritmo FIJO de cada endpoint del ABM:
 *     preUseCase(...)  ->  llamada al BaseService  ->  postUseCase(...)  ->  respuesta HTTP
 * Los controllers concretos solo indican la URL (@RequestMapping) y, si lo necesitan,
 * redefinen los hooks.
 *
 *   GET    /           listar activos
 *   GET    /{id}       consultar
 *   POST   /           alta
 *   PUT    /{id}       modificacion
 *   DELETE /{id}       baja logica
 */
public abstract class BaseController<T extends BaseEntity<ID>, ID> {

    protected final BaseService<T, ID> service;

    protected BaseController(BaseService<T, ID> service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<T>> listar() throws ErrorServiceException {
        preUseCase(BaseUseCaseController.LISTAR, null, null);
        List<T> lista = service.listarActivos();
        postUseCase(BaseUseCaseController.LISTAR, null);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<T> consultar(@PathVariable ID id) throws ErrorServiceException {
        preUseCase(BaseUseCaseController.CONSULTAR, id, null);
        T entidad = service.obtenerEntidad(id);
        postUseCase(BaseUseCaseController.CONSULTAR, entidad);
        return ResponseEntity.ok(entidad);
    }

    @PostMapping
    public ResponseEntity<T> alta(@RequestBody T entidad) throws ErrorServiceException {
        preUseCase(BaseUseCaseController.ALTA, null, entidad);
        T creada = service.alta(entidad);
        postUseCase(BaseUseCaseController.ALTA, creada);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<T> modificar(@PathVariable ID id, @RequestBody T entidad) throws ErrorServiceException {
        preUseCase(BaseUseCaseController.MODIFICACION, id, entidad);
        T modificada = service.modificar(id, entidad);
        postUseCase(BaseUseCaseController.MODIFICACION, modificada);
        return ResponseEntity.ok(modificada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> baja(@PathVariable ID id) throws ErrorServiceException {
        preUseCase(BaseUseCaseController.BAJA, id, null);
        service.baja(id);
        postUseCase(BaseUseCaseController.BAJA, null);
        return ResponseEntity.noContent().build();
    }

    // ============================== HOOKS ==============================
    // Para redefinir en los controllers que heredan (acciones previas / posteriores).
    protected void preUseCase(BaseUseCaseController useCase, ID id, T entidad) throws ErrorServiceException {}
    protected void postUseCase(BaseUseCaseController useCase, T entidad) throws ErrorServiceException {}
}
