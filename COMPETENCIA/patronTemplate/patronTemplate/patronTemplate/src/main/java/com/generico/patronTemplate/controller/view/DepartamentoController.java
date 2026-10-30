package com.generico.patronTemplate.controller.view;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.generico.patronTemplate.business.logic.error.ErrorServiceException;
import com.generico.patronTemplate.business.logic.service.DepartamentoService;
import com.generico.patronTemplate.business.logic.service.PaisService;
import com.generico.patronTemplate.business.domain.entity.Departamento;

@Controller
@RequestMapping("/departamento")
public class DepartamentoController extends BaseController<Departamento, Long> {
	
	@Autowired private PaisService paisService;
	
	public DepartamentoController(DepartamentoService service, ApplicationContext context) {
        super(service, context);
        initController(new Departamento(), "LIST DEPARTAMENTO", "EDIT DEPARTAMENTO");
    }

	@Override
	protected void preUseCase(BaseUseCaseController useCase)throws ErrorServiceException{
	   this.model.addAttribute("paises", paisService.listarActivos());
	   this.model.addAttribute("provincias", new ArrayList());
	}
	
}
