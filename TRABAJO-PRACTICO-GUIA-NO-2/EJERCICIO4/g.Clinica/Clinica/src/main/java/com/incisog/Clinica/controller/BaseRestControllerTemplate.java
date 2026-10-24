package com.incisog.Clinica.controller;

import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.service.BaseServiceTemplate;
import com.incisog.Clinica.models.BaseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class BaseRestControllerTemplate<T extends BaseEntity, D, ID> {

//    protected final BaseServiceTemplate<T, D, ID> baseService;
//
//    protected BaseRestControllerTemplate(BaseServiceTemplate<T, D, ID> baseService) {
//        this.baseService = baseService;
//    }
//
//    @ExceptionHandler(ErrorServiceException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ResponseEntity<String> handleServiceException(ErrorServiceException ex) {
//        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
//    }
//
//    @GetMapping
//    public ResponseEntity<List<D>> listarActivos() throws ErrorServiceException {
//        List<D> dtos = baseService.listarActivos();
//        return ResponseEntity.ok(dtos);
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<D> obtener(@PathVariable ID id) throws ErrorServiceException {
//        return baseService.obtener(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @PostMapping
//    public ResponseEntity<D> alta(@RequestBody D dto) throws ErrorServiceException {
//        D nuevoDto = baseService.alta(dto);
//        return new ResponseEntity<>(nuevoDto, HttpStatus.CREATED);
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<D> modificar(@PathVariable ID id, @RequestBody D dto) throws ErrorServiceException {
//        return baseService.modificar(id, dto)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> baja(@PathVariable ID id) throws ErrorServiceException {
//        baseService.baja(id);
//        return ResponseEntity.noContent().build();
//    }
}
