package com.generico.patronTemplate.controller.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.generico.patronTemplate.business.logic.error.ErrorServiceException;
import com.generico.patronTemplate.business.logic.service.PaisService;
import com.generico.patronTemplate.business.logic.service.ProvinciaService;
import com.generico.patronTemplate.business.domain.entity.Provincia;

@Controller
@RequestMapping("/provincia")
public class ProvinciaController extends BaseController<Provincia, Long> {
	
	@Autowired
   	private PaisService paisService;
	
	public ProvinciaController(ProvinciaService service, ApplicationContext context) {
        super(service, context);
        initController(new Provincia(), "LIST PROVINCIA", "EDIT PROVINCIA");
    }

	@Override
	protected void preUseCase(BaseUseCaseController useCase)throws ErrorServiceException{
	   this.model.addAttribute("paises", paisService.listarActivos());
	}
	
}
