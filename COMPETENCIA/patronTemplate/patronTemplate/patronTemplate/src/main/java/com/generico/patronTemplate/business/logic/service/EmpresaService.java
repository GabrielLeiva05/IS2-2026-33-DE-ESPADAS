package com.generico.patronTemplate.business.logic.service;

import org.springframework.stereotype.Service;

import com.generico.patronTemplate.business.domain.entity.Empresa;
import com.generico.patronTemplate.business.logic.error.ErrorServiceException;
import com.generico.patronTemplate.business.percistence.repository.EmpresaRepository;

@Service
public class EmpresaService extends BaseService<Empresa, Long> {
    
	public EmpresaService(EmpresaRepository repository) {
        super(repository);
    }
    
	@Override
	protected void validar(BaseUseCaseService useCase,  Empresa empresa) throws ErrorServiceException {
		
	   try {	
			 
		 if (useCase != BaseUseCaseService.BAJA) { 
			 
		   if (empresa == null) {
			  throw new ErrorServiceException("Debe indicar la empresa");	   
	       }
		   
		   if (empresa.getRazonSocial() == null || empresa.getRazonSocial().trim().isEmpty()) {
			  throw new ErrorServiceException("Debe indicar la razón social");
		   }
		   
		   if (empresa.getCuit() == null || empresa.getCuit().trim().isEmpty()) {
			  throw new ErrorServiceException("Debe indicar el cuit");
		   }
		   
		   if (empresa.getTelefono() == null || empresa.getTelefono().trim().isEmpty()) {
		      throw new ErrorServiceException("Debe indicar el teléfono");
		   }
		   
		   if (empresa.isEliminado()) {
		      throw new ErrorServiceException("La empresa indicada se encuentra eliminado");	   
		   }
		   
		   Empresa empresaExistente = ((EmpresaRepository) repository).buscarEmpresaPorCuit(empresa.getCuit());
		   if ((empresaExistente != null && !empresaExistente.isEliminado() && useCase == BaseUseCaseService.ALTA) ||
			   (empresaExistente != null && !empresaExistente.isEliminado() && !empresaExistente.getId().equals(empresa.getId()) && useCase == BaseUseCaseService.MODIFICACION)   
			  ) {
			   throw new ErrorServiceException("Existe una empresa con el cuit indicado");    
           }
		 } 
		   
	   }catch(ErrorServiceException e) {	 
		 throw e;  
	   }catch(Exception e) {
		 throw new ErrorServiceException("Error de Sistemas");  
	   }  
	}
    
}
