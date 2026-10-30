package com.generico.patronTemplate.controller.view;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.generico.patronTemplate.business.logic.service.PaisService;
import com.generico.patronTemplate.business.domain.entity.Pais;

@Controller
@RequestMapping("/pais")
public class PaisController extends BaseController<Pais, Long> {
	
	public PaisController(PaisService service, ApplicationContext context) {
        super(service, context);
        initController(new Pais(), "LIST PAÍS", "EDIT PAÍS");
    }
	

}
