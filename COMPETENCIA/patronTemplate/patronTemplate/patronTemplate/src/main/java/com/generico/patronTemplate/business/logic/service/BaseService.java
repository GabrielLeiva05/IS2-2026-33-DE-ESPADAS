package com.generico.patronTemplate.business.logic.service;

import java.util.List;
import java.util.Optional;

import com.generico.patronTemplate.business.domain.entity.BaseEntity;
import com.generico.patronTemplate.business.percistence.repository.BaseRepository;
import com.generico.patronTemplate.business.logic.error.ErrorServiceException;

public abstract class BaseService<T extends BaseEntity<ID>, ID> {

    protected final BaseRepository<T, ID> repository;

    protected BaseService(BaseRepository<T, ID> repository) {
        this.repository = repository;
    }

    public T alta(T entidad) throws ErrorServiceException {
      try {		
    	  
    	entidad.setEliminado(false);
    	validar(BaseUseCaseService.ALTA, entidad);
        preAlta(entidad);
        
        T guardado = repository.save(entidad);
        
        postAlta(guardado);
        return guardado;
        
      }catch(ErrorServiceException e) {
    	throw e; 
      }catch(Exception e) {
      	throw new ErrorServiceException("Error de Sistemas");  
      }   
    }

    public Optional<T> modificar(ID id, T entidadNueva)throws ErrorServiceException {
      try {	
    	  
    	entidadNueva.setId(id);
    	validar(BaseUseCaseService.MODIFICACION,entidadNueva);
        preModificacion(entidadNueva);
        
        return repository.findById(id).map(entidad -> {
        	entidadNueva.setId(id);
            T actualizado = repository.save(entidadNueva);
            return actualizado;
        });
        
      }catch(ErrorServiceException e) {
      	throw e; 
      }catch(Exception e) {
    	throw new ErrorServiceException("Error de Sistemas");  
      }   
    }

    public void baja(ID id)throws ErrorServiceException {
      try {		
    	  
    	T entidadBaja = obtenerEntidad(id);  
    	validar(BaseUseCaseService.BAJA,entidadBaja);
    	entidadBaja.setEliminado(true);
    	repository.save(entidadBaja);
    	
      }catch(ErrorServiceException e) {
      	throw e; 
      }catch(Exception e) {
  		throw new ErrorServiceException("Error de Sistemas");  
  	  }  
    }

    public Optional<T> obtener(ID id)throws ErrorServiceException {
      try {	 	
    	  
        return repository.findById(id).
        	              filter(e -> !Boolean.TRUE.equals(e.isEliminado()));
        
      }catch(Exception e) {
    	throw new ErrorServiceException("Error de Sistemas");  
      } 
    }
    
    public T obtenerEntidad(ID id) throws ErrorServiceException {
        try {
            return repository.findById(id)
                             .filter(e -> !Boolean.TRUE.equals(e.isEliminado()))
                             .orElseThrow(() -> new ErrorServiceException("Entidad no encontrada o eliminada."));
        } catch (Exception e) {
            throw new ErrorServiceException("Error de Sistemas");
        }
    }

    public List<T> listarActivos()throws ErrorServiceException {
      try {	
    	  
        return repository.findAll().stream()
                         .filter(e -> !Boolean.TRUE.equals(e.isEliminado()))
                         .toList(); 
        
	  }catch(Exception e) {
		throw new ErrorServiceException("Error de Sistemas");  
	  }  
    }
    
    

    //Metodos para ser redefinidos en las clases de servicio que heredan, con el objetivo
    //que sea necesario realizar acciones previas o posteriores en las Altas, Bajas y
    //Modificaciones.
    //Se deberá redefinir el comportamiento en la clase que hereda.
    protected void validar(BaseUseCaseService useCase, T entidad) throws ErrorServiceException {}
    protected void preAlta(T entidad) throws ErrorServiceException {}
    protected void postAlta(T entidad)throws ErrorServiceException {}
    protected void preModificacion(T entidad)throws ErrorServiceException {}
    protected void preBaja(T entidad)throws ErrorServiceException {}
}