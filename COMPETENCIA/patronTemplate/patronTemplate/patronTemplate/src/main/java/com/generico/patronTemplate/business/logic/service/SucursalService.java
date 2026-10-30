package com.generico.patronTemplate.business.logic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.generico.patronTemplate.business.domain.entity.Sucursal;
import com.generico.patronTemplate.business.logic.error.ErrorServiceException;
import com.generico.patronTemplate.business.percistence.repository.SucursalRepository;

@Service
public class SucursalService extends BaseService<Sucursal, Long> {
    
	@Autowired 
	private DireccionService dieccionService;
	
	public SucursalService(SucursalRepository repository) {
        super(repository);
    }
    
	@Override
	protected void validar(BaseUseCaseService useCase,  Sucursal sucursal) throws ErrorServiceException {
		
	   try {	
			 
		 if (useCase != BaseUseCaseService.BAJA) { 
			 
		   if (sucursal == null) {
			  throw new ErrorServiceException("Debe indicar la sucursal");	   
	       }
		   
		   if (sucursal.getNombre() == null || sucursal.getNombre().trim().isEmpty()) {
			  throw new ErrorServiceException("Debe indicar el nombre");
		   }
		   
		   if (sucursal.getEmpresa() == null) {
			  throw new ErrorServiceException("Debe indicar la empresa");
		   }
		   
		   if (sucursal.isEliminado()) {
		      throw new ErrorServiceException("La sucursal indicada se encuentra eliminado");	   
		   }
		   
		   dieccionService.validarDireccion(useCase, sucursal.getDireccion() == null ? null : sucursal.getDireccion());
		   
		 } 
		   
	   }catch(ErrorServiceException e) {	 
		 throw e;  
	   }catch(Exception e) {
		 throw new ErrorServiceException("Error de Sistemas");  
	   }  
	}
    
}
