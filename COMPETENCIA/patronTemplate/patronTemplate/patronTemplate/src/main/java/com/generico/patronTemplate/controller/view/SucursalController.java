package com.generico.patronTemplate.controller.view;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.generico.patronTemplate.business.logic.error.ErrorServiceException;
import com.generico.patronTemplate.business.logic.service.EmpresaService;
import com.generico.patronTemplate.business.logic.service.PaisService;
import com.generico.patronTemplate.business.logic.service.SucursalService;
import com.generico.patronTemplate.business.domain.entity.Localidad;
import com.generico.patronTemplate.business.domain.entity.Sucursal;

@Controller
@RequestMapping("/sucursal")
public class SucursalController extends BaseController<Sucursal, Long> {
	
	@Autowired 
	private PaisService paisService;
	@Autowired 
	private EmpresaService empresaService;
	
	public SucursalController(SucursalService service, ApplicationContext context) {
        super(service, context);
        initController(new Sucursal(), "LIST SUCURSAL", "EDIT SUCURSAL");
    }

	@Override
	protected void preUseCase(BaseUseCaseController useCase)throws ErrorServiceException{
	   this.model.addAttribute("empresas", empresaService.listarActivos());	
	   this.model.addAttribute("paises", paisService.listarActivos());
	   this.model.addAttribute("provincias", new ArrayList());
	   this.model.addAttribute("departamentos", new ArrayList());
	   this.model.addAttribute("localidades", new ArrayList<Localidad>());
	}
	
}
