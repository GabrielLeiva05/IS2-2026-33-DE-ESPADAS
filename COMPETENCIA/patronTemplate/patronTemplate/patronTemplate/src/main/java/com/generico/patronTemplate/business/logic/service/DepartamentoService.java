package com.generico.patronTemplate.business.logic.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.generico.patronTemplate.business.domain.entity.Departamento;
import com.generico.patronTemplate.business.logic.error.ErrorServiceException;
import com.generico.patronTemplate.business.percistence.repository.DepartamentoRepository;

@Service
public class DepartamentoService extends BaseService<Departamento, Long> {

	public DepartamentoService(DepartamentoRepository repository) {
		super(repository);
	}

	@Override
	protected void validar(BaseUseCaseService useCase, Departamento departamento) throws ErrorServiceException {

		try {

			if (departamento == null) {
				throw new ErrorServiceException("Debe indicar el departamento");
			}

			if (departamento.getNombre() == null || departamento.getNombre().trim().isEmpty()) {
				throw new ErrorServiceException("Debe indicar el nombre del departamento");
			}
			
			if (departamento.isEliminado()) {
				throw new ErrorServiceException("El departamento indicado se encuentra eliminado");	   
			}

			if (departamento.getProvincia() == null) {
				throw new ErrorServiceException("Debe indicar la provincia");
			}
			
			Departamento departamentoExsitente = ((DepartamentoRepository) repository).buscarDepartamentoPorProvinciaYNombre(departamento.getProvincia().getId(), departamento.getNombre());
			if ((departamentoExsitente != null && !departamentoExsitente.isEliminado() && useCase == BaseUseCaseService.ALTA) ||
			       (departamentoExsitente != null && !departamentoExsitente.isEliminado() && !departamentoExsitente.getId().equals(departamentoExsitente.getId()) && useCase == BaseUseCaseService.MODIFICACION)   
			    ) {
			       throw new ErrorServiceException("Existe una departamento con el nombre indicado");    
			}

		} catch (ErrorServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ErrorServiceException("Error de Sistemas");
		}
	}

	public List<Departamento> listarDepartamentoPorProvinciaActivo(Long id) throws ErrorServiceException {
		try {

			if (id == null) {
				throw new ErrorServiceException("Debe indicar el país");
			}

			return ((DepartamentoRepository) repository).listarDepartamentoActivo(id);

		} catch (ErrorServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ErrorServiceException("Error de Sistemas");
		}
	}

}
