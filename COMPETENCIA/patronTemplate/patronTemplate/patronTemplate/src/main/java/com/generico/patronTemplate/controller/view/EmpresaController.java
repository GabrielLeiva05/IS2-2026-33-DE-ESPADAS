package com.generico.patronTemplate.controller.view;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.generico.patronTemplate.business.logic.service.EmpresaService;
import com.generico.patronTemplate.business.domain.entity.Empresa;

@Controller
@RequestMapping("/empresa")
public class EmpresaController extends BaseController<Empresa, Long> {
	
	public EmpresaController(EmpresaService service, ApplicationContext context) {
        super(service, context);
        initController(new Empresa(), "LIST EMPRESA", "EDIT EMPRESA");
    }
	

}
