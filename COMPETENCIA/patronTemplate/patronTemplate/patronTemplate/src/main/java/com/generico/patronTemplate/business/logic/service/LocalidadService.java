package com.generico.patronTemplate.business.logic.service;

import org.springframework.stereotype.Service;

import com.generico.patronTemplate.business.domain.entity.Localidad;
import com.generico.patronTemplate.business.logic.error.ErrorServiceException;
import com.generico.patronTemplate.business.percistence.repository.LocalidadRepository;

@Service
public class LocalidadService extends BaseService<Localidad, Long> {
    
	public LocalidadService(LocalidadRepository repository) {
        super(repository);
    }
    
	@Override
	protected void validar(BaseUseCaseService useCase, Localidad localidad) throws ErrorServiceException {
		
	   try {	
			 
		  if (useCase != BaseUseCaseService.BAJA) { 
			  
		   if (localidad == null) {
			   throw new ErrorServiceException("Debe indicar la localidad");  
		   }
		   
		   if (localidad.getNombre() == null || localidad.getNombre().trim().isEmpty()) {
			  throw new ErrorServiceException("Debe indicar el nombre de la localidad");
		   }
		   
		   if (localidad.getCodigoPostal() == null || localidad.getCodigoPostal().trim().isEmpty()) {
			  throw new ErrorServiceException("Debe indicar el nombre código postal");
		   }
		   
		   if (localidad.getDepartamento() == null || localidad.getDepartamento().isEliminado()) {
			  throw new ErrorServiceException("El departamento de la localidad indicada es incorrecto");
		   }
		   
		   Localidad localidadExsitente = ((LocalidadRepository) repository).buscarLocalidadPorDepartamentoYNombre(localidad.getDepartamento().getId(), localidad.getNombre());
		   if ((localidadExsitente != null && !localidadExsitente.isEliminado() && useCase == BaseUseCaseService.ALTA) ||
		       (localidadExsitente != null && !localidadExsitente.isEliminado() && !localidadExsitente.getId().equals(localidadExsitente.getId()) && useCase == BaseUseCaseService.MODIFICACION)   
		      ) {
		       throw new ErrorServiceException("Existe una localidad con el nombre indicado");    
		   }
		  } 
		   
	   }catch(ErrorServiceException e) {	 
		 throw e;  
	   }catch(Exception e) {
		 throw new ErrorServiceException("Error de Sistemas");  
	   }  
	}
    
}
