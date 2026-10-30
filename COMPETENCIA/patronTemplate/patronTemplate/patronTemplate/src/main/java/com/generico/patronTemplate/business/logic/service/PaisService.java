package com.generico.patronTemplate.business.logic.service;

import org.springframework.stereotype.Service;

import com.generico.patronTemplate.business.domain.entity.Pais;
import com.generico.patronTemplate.business.logic.error.ErrorServiceException;
import com.generico.patronTemplate.business.percistence.repository.PaisRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaisService extends BaseService<Pais, Long> {
    
	public PaisService(PaisRepository repository) {
        super(repository);
    }
    
	@Override
	protected void validar(BaseUseCaseService useCase,  Pais pais) throws ErrorServiceException {
		
	   try {	
			 
		 if (useCase != BaseUseCaseService.BAJA) { 
			 
		   if (pais.getNombre() == null) {
			  throw new ErrorServiceException("Debe indicar el país");	   
	       }
		   
		   if (pais.getNombre() == null || pais.getNombre().trim().isEmpty()) {
			  throw new ErrorServiceException("Debe indicar el nombre del país");
		   }
		   
		   if (pais.isEliminado()) {
		      throw new ErrorServiceException("El país indicado se encuentra eliminado");	   
		   }
		   
		   Pais paisExsitente = ((PaisRepository) repository).buscarPaisPorNombre(pais.getNombre());
		   if ((paisExsitente != null && !paisExsitente.isEliminado() && useCase == BaseUseCaseService.ALTA) ||
			   (paisExsitente != null && !paisExsitente.isEliminado() && !paisExsitente.getId().equals(pais.getId()) && useCase == BaseUseCaseService.MODIFICACION)   
			  ) {
			   throw new ErrorServiceException("Existe un país con el nombre indicado");    
           }
		 } 
		   
	   }catch(ErrorServiceException e) {	 
		 throw e;  
	   }catch(Exception e) {
		 throw new ErrorServiceException("Error de Sistemas");  
	   }  
	}
    
}
