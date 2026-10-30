package com.generico.patronTemplate.controller.view;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.generico.patronTemplate.business.logic.error.ErrorServiceException;
import com.generico.patronTemplate.business.logic.service.LocalidadService;
import com.generico.patronTemplate.business.logic.service.PaisService;
import com.generico.patronTemplate.business.domain.entity.Localidad;

@Controller
@RequestMapping("/localidad")
public class LocalidadController extends BaseController<Localidad, Long> {
	
	@Autowired 
	private PaisService paisService;
	
	public LocalidadController(LocalidadService service, ApplicationContext context) {
        super(service, context);
        initController(new Localidad(), "LIST LOCALIDAD", "EDIT LOCALIDAD");
    }

	@Override
	protected void preUseCase(BaseUseCaseController useCase)throws ErrorServiceException{
	   this.model.addAttribute("paises", paisService.listarActivos());
	   this.model.addAttribute("provincias", new ArrayList());
	   this.model.addAttribute("departamentos", new ArrayList());
	}
	
	
}
