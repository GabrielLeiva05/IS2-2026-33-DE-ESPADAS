package com.generico.patronTemplate.business.logic.service;

import org.springframework.stereotype.Service;

import com.generico.patronTemplate.business.domain.entity.Direccion;
import com.generico.patronTemplate.business.logic.error.ErrorServiceException;
import com.generico.patronTemplate.business.percistence.repository.DireccionRepository;

@Service
public class DireccionService extends BaseService<Direccion, Long> {
    
	public DireccionService(DireccionRepository repository) {
        super(repository);
    }
    
	public Direccion validarDireccion(BaseUseCaseService useCase, Direccion direccion) throws ErrorServiceException {
		
	   try {	
			 
		 if (useCase != BaseUseCaseService.BAJA) { 
			 
		   if (direccion == null) {
			  throw new ErrorServiceException("Debe indicar la dirección");	   
	       }
		   
		   if (direccion.getCalle() == null || direccion.getCalle().trim().isEmpty()){
               throw new ErrorServiceException("Debe indicar la calle");  
            }
            
            if (direccion.getNumeracion()== null || direccion.getNumeracion().trim().isEmpty()){
               throw new ErrorServiceException("Debe indicar la numeración");  
            }
            
            if (direccion.getBarrio() == null || direccion.getBarrio().trim().isEmpty()){
               throw new ErrorServiceException("Debe indicar el barrio");  
            }
            
            if (direccion.getPisoCasa() == null || direccion.getPisoCasa().trim().isEmpty()){
               throw new ErrorServiceException("Debe indicar el piso / casa");  
            }
           
            if (direccion.getPuertaManzana() == null || direccion.getPuertaManzana().trim().isEmpty()){
               throw new ErrorServiceException("Debe indicar el puerta / manzana");  
            }
            
            if (direccion.getUbicacionCoordenadaX() == null || direccion.getUbicacionCoordenadaX().trim().isEmpty()){
               throw new ErrorServiceException("Debe indicar la ubicaciónCoordenadaX");  
            }
            
            if (direccion.getUbicacionCoordenadaY() == null || direccion.getUbicacionCoordenadaY().trim().isEmpty()){
               throw new ErrorServiceException("Debe indicar la ubicaciónCoordenadaY");  
            }
		 } 
		 
		 return direccion;
		   
	   }catch(ErrorServiceException e) {	 
		 throw e;  
	   }catch(Exception e) {
		 throw new ErrorServiceException("Error de Sistemas");  
	   }  
	}
    
}
