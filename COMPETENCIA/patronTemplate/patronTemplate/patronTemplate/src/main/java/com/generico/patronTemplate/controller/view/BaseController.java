package com.generico.patronTemplate.controller.view;

import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.generico.patronTemplate.business.logic.error.ErrorServiceException;
import com.generico.patronTemplate.business.domain.entity.BaseEntity;
import com.generico.patronTemplate.business.logic.service.BaseService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseController<T extends BaseEntity<ID>, ID> {

    protected final BaseService<T, ID> service;
    private final ApplicationContext context;
    private String nameClass="";
    protected String viewList="";
    protected String redirectList="";
    protected String viewEdit="";
    protected T entity;
    protected String nameEntityLower;
    protected Model model;
    protected String titleList;
    protected String titleEdit;
    protected String errorMenssage;

    protected BaseController(BaseService<T, ID> service, ApplicationContext context) {
        this.service = service;
        this.context = context;
    }

    protected void initController(T entity, String titleList, String titleEdit) {
    	this.entity= entity;
    	this.nameClass = getNameEntity(this.entity);
    	this.nameEntityLower = nameClass.toLowerCase();
        this.viewList= "view/l"+ nameClass +".html";
        this.redirectList= "redirect:/"+nameEntityLower+"/list";
        this.viewEdit= "view/e"+ nameClass +".html";
        this.titleList= titleList;
        this.titleEdit= titleEdit;
    }
    
    private String getNameEntity(T object){
        return ((((T) object).getClass()).getSimpleName());
    }
    
    @GetMapping("/list")
    public String list(Model model) {
      try {	
    	  
    	this.model =model;   
        List<T> listEntity = service.listarActivos();
        this.model.addAttribute("items", listEntity);
        this.model.addAttribute("titleList", titleList);
        this.model.addAttribute("nameEntityLower", nameEntityLower);
        
      }catch(ErrorServiceException e) {	
    	  showErrorMenssage(e.getMessage(), BaseUseCaseController.LISTAR);
	  }catch(Exception e) {
		  this.model.addAttribute("msgError", "Error de Sistema");
		  showErrorMenssage(e.getMessage(), BaseUseCaseController.LISTAR);
	  }
      
      return viewList;
    }

    @GetMapping("/alta")
    public String crear(T entity, Model model) {
      try {	
    	  
    	this.model=model;
    	this.entity = entity;
    	this.model.addAttribute("item", this.entity);
    	this.model.addAttribute("isDisabled", false);
    	this.model.addAttribute("titleEdit", titleEdit);
    	this.model.addAttribute("nameEntityLower", nameEntityLower);
    	
    	preUseCase(BaseUseCaseController.ALTA);
    	
        return viewEdit;
        
      }catch(ErrorServiceException e) {	
    	  showErrorMenssage(e.getMessage(), BaseUseCaseController.ALTA);
		  return viewEdit;
	  }catch(Exception e) {
		  showErrorMenssage(e.getMessage(), BaseUseCaseController.ALTA);
		  return viewEdit;
	  }   
    }

    @GetMapping("/consultar/{id}")
    public String consultar(@PathVariable ID id, Model model) {
      try {
    	  
    	this.model=model;
      	this.entity = service.obtener(id).orElseThrow(() -> new IllegalArgumentException("No encontrado: " + id));
      	this.model.addAttribute("item", entity);
      	this.model.addAttribute("isDisabled", true);
      	this.model.addAttribute("titleEdit", titleEdit);
      	this.model.addAttribute("nameEntityLower", nameEntityLower);
      	
      	preUseCase(BaseUseCaseController.CONSULTAR);
      	
        return viewEdit;
        
      }catch(ErrorServiceException e) {	
    	  showErrorMenssage(e.getMessage(), BaseUseCaseController.CONSULTAR);
		  return viewEdit;
	  }catch(Exception e) {
		  showErrorMenssage(e.getMessage(), BaseUseCaseController.CONSULTAR);
		  return viewEdit;
	  } 
    }
    
    @GetMapping("/modificar/{id}")
    public String editar(@PathVariable ID id, Model model) {
      try {
    	  
    	this.model=model;
      	this.entity = service.obtener(id).orElseThrow(() -> new IllegalArgumentException("No encontrado: " + id));
      	this.model.addAttribute("item", entity);
      	this.model.addAttribute("isDisabled", false);
      	this.model.addAttribute("titleEdit", titleEdit);
      	this.model.addAttribute("nameEntityLower", nameEntityLower);
        preUseCase(BaseUseCaseController.MODIFICACION);
      	
        return viewEdit;
        
      }catch(ErrorServiceException e) {	
    	  showErrorMenssage(e.getMessage(), BaseUseCaseController.MODIFICACION);
		  return viewEdit;
	  }catch(Exception e) {
		  showErrorMenssage(e.getMessage(), BaseUseCaseController.MODIFICACION);
		  return viewEdit;
	  } 
    }
    
    @GetMapping("/baja/{id}")
    public String eliminar(@PathVariable ID id, RedirectAttributes attributes, Model model) {
      try {  
    	  
    	this.model=model;
    	
    	preUseCase(BaseUseCaseController.BAJA);
    	service.baja(id);
    	
    	attributes.addFlashAttribute("msgExito", "La acción fue realizada correctamente.");
        return redirectList;
        
      }catch(ErrorServiceException e) {	
    	  showErrorMenssage(e.getMessage(), BaseUseCaseController.BAJA);
		  return viewEdit;
	  }catch(Exception e) {
		  showErrorMenssage(e.getMessage(), BaseUseCaseController.BAJA);
		  return viewEdit;
	  }   
    }

    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute("item") T entity, RedirectAttributes attributes, Model model) {
      try { 
    	  
    	this.model=model;
    	this.entity = entity; 
    	preUseCase(BaseUseCaseController.ACTUALIZAR);
    	
    	if(this.entity.getId() == null)
    	 service.alta(this.entity);
        else
    	 service.modificar(this.entity.getId(), this.entity);
    	
    	preUseCase(BaseUseCaseController.ACTUALIZAR);
    	
        attributes.addFlashAttribute("msgExito", "La acción fue realizada correctamente.");
        return redirectList;
        
      }catch(ErrorServiceException e) {	
    	  showErrorMenssage(e.getMessage(), BaseUseCaseController.ACTUALIZAR);
		  return viewEdit;
	  }catch(Exception e) {
		  showErrorMenssage(e.getMessage(), BaseUseCaseController.ACTUALIZAR);
		  return viewEdit;
	  } 
    }
    
    @GetMapping("/cancelar")
	public String cancelar() {
		return redirectList;             
	}
    
    protected String showErrorMenssage(String mensaje, BaseUseCaseController useCase){
    	
    	try {
    		
    		this.model.addAttribute("titleEdit", titleEdit);
    		this.model.addAttribute("nameEntityLower", nameEntityLower);
    		this.model.addAttribute("msgError", mensaje);
    		
    		if (this.entity.getId() != null) {
    		  this.model.addAttribute("item", service.obtenerEntidad(this.entity.getId()));	
    		}else {		
    		  this.model.addAttribute("item", loadingEntityForFormByError(useCase, model, this.entity));			
    		}
    		
    	}catch(Exception e) {}
    	
    	return viewEdit;
    }
    
    
    /*
     * Método genérico utilizado para filtrar por cascada en los combo box.
     * Parámetros Entidad que se consultar, nombreAtributoSuperClass por el que se va filtrara y idFiltro por
     * el que se va filtrara.
     * Ej.: Si quiero filtrar las los paises de una provincia los parametros son:
     * Entidad: Provincia, nombreAtributoSuperClass: pais (el atributo de la superclase en la subclase), 
     * idFiltro: idPais 
     */
    @GetMapping("/filterComboBox")
	public ResponseEntity<?>filterForComboBox(@RequestParam String entidad, @RequestParam String nombreAtributoSuperClass, @RequestParam Long idFiltro) throws ErrorServiceException {
		 
      try {
    	  
    	 //Obtener clase e instancia del serivicio utilizando reflection
    	 String paqueteEntidades = "com.generico.patronTemplate.business.domain.entity.";
         Class<?> claseEntidad = Class.forName(paqueteEntidades + entidad);
         BaseService<?, ID> service=(BaseService<?, ID>) this.context.getBean(entidad.toLowerCase() + "Service");
		
		 // Filtrar dinámicamente
         List<?> resultados = service.listarActivos().stream()
             .filter(obj -> {
                 try {
                     Field campo = claseEntidad.getDeclaredField(nombreAtributoSuperClass);
                     campo.setAccessible(true);
                     Object origen = campo.get(obj);
                     Method getId = origen.getClass().getMethod("getId");
                     Long id = (Long) getId.invoke(origen);
                     return id.equals(idFiltro);
                 } catch (Exception e) {
                     return false;
                 }
             }).collect(Collectors.toList());

         return ResponseEntity.ok(resultados);
         
      } catch (Exception e) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al procesar la solicitud: " + e.getMessage());
      } 
	}

    
    //Metodos para ser redefinidos en los controladores que heredan, con el objetivo
    //que sea necesario realizar acciones previas o posteriores en las Altas, Bajas y
    //Modificaciones.
    //Se deberá redefinir el comportamiento en la clase que hereda.
    protected void preUseCase(BaseUseCaseController useCase)throws ErrorServiceException {}
    protected void postUseCase(BaseUseCaseController useCase)throws ErrorServiceException {}
    protected T loadingEntityForFormByError(BaseUseCaseController useCase, Model model, T entity)throws ErrorServiceException { return entity;}
    
}