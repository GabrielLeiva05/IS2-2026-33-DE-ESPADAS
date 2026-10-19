package com.tienda.carrito.controller.view;

import com.tienda.carrito.business.domain.entity.BaseEntity;
import com.tienda.carrito.business.logic.error.ErrorServiceException;
import com.tienda.carrito.business.logic.service.BaseService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * CONTROLADOR BASE - Template Method en la capa de control (misma estructura que patronTemplate).
 *
 * Define una sola vez el flujo CRUD de pantallas:
 *   GET  /list            -> vista lXxx (listado)
 *   GET  /alta            -> vista eXxx (formulario vacio)
 *   GET  /consultar/{id}  -> vista eXxx (solo lectura)
 *   GET  /modificar/{id}  -> vista eXxx (editable)
 *   POST /actualizar      -> alta o modificacion segun el formulario traiga id
 *   POST /baja/{id}       -> baja logica
 *   GET  /cancelar        -> vuelve al listado
 *
 * Cada controlador hijo llama a initController(entidadVacia, tituloListado, tituloFormulario) y, si hace falta,
 * redefine los hooks preUseCase() / postUseCase() / ejecutarActualizacion() / ejecutarBaja().
 *
 * Regla: el controlador NO tiene reglas de negocio; solo delega en el servicio y elige la vista.
 *
 * DIFERENCIA con patronTemplate: alli el controlador guardaba el Model y la entidad en CAMPOS de la clase.
 * Un controlador es un singleton compartido por todos los usuarios, asi que dos peticiones simultaneas se
 * pisaban entre si (un usuario podia ver datos de otro). Aca esos datos viajan como PARAMETROS de cada metodo;
 * los unicos campos son la configuracion fija que se establece una sola vez en initController().
 */
public abstract class BaseController<T extends BaseEntity<ID>, ID> {

    private static final Logger log = LoggerFactory.getLogger(BaseController.class);

    protected final BaseService<T, ID> service;

    // Configuracion fija (se completa una sola vez en initController)
    private String nameEntityLower;
    private String basePath;
    private String viewList;
    private String viewEdit;
    private String redirectList;
    private String titleList;
    private String titleEdit;

    protected BaseController(BaseService<T, ID> service) {
        this.service = service;
    }

    protected void initController(T entity, String titleList, String titleEdit) {
        String nameClass = entity.getClass().getSimpleName();
        this.nameEntityLower = nameClass.toLowerCase();
        this.viewList = "view/l" + nameClass;
        this.viewEdit = "view/e" + nameClass;
        this.titleList = titleList;
        this.titleEdit = titleEdit;

        RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(getClass(), RequestMapping.class);
        this.basePath = (mapping != null && mapping.value().length > 0) ? mapping.value()[0] : "/" + nameEntityLower;
        this.redirectList = "redirect:" + basePath + "/list";
    }

    // ===================== Hooks (los redefine cada controlador si los necesita) =====================

    /** Se ejecuta antes de armar la vista o de ejecutar la accion. Ideal para cargar combos en el Model. */
    protected void preUseCase(BaseUseCaseController useCase, Model model, T entity) throws ErrorServiceException { }

    protected void postUseCase(BaseUseCaseController useCase, Model model, T entity) throws ErrorServiceException { }

    /** Paso variable de "actualizar": por defecto alta o modificacion segun el formulario traiga id. */
    protected void ejecutarActualizacion(T entity, List<MultipartFile> archivos) throws ErrorServiceException {
        if (esNuevo(entity)) {
            service.alta(entity);
        } else {
            service.modificar(entity.getId(), entity)
                    .orElseThrow(() -> new ErrorServiceException("No se encontró el registro a modificar."));
        }
    }

    /** Paso variable de "baja": por defecto baja logica por id. */
    protected void ejecutarBaja(ID id, HttpSession sesion) throws ErrorServiceException {
        service.baja(id);
    }

    // ===================== Endpoints =====================

    @GetMapping("/list")
    public String list(Model model) {
        try {
            model.addAttribute("items", service.listarActivos());
            model.addAttribute("titleList", titleList);
            model.addAttribute("basePath", basePath);
            model.addAttribute("nameEntityLower", nameEntityLower);
            return viewList;
        } catch (ErrorServiceException e) {
            return showErrorMenssage(e.getMessage(), BaseUseCaseController.LISTAR, model, null);
        } catch (Exception e) {
            log.error("Error en list", e);
            return showErrorMenssage("Error de Sistemas", BaseUseCaseController.LISTAR, model, null);
        }
    }

    @GetMapping("/alta")
    public String crear(@ModelAttribute("item") T entity, Model model) {
        try {
            preUseCase(BaseUseCaseController.ALTA, model, entity);
            return showForm(model, entity, false);
        } catch (ErrorServiceException e) {
            return showErrorMenssage(e.getMessage(), BaseUseCaseController.ALTA, model, entity);
        } catch (Exception e) {
            log.error("Error en alta (pantalla)", e);
            return showErrorMenssage("Error de Sistemas", BaseUseCaseController.ALTA, model, entity);
        }
    }

    @GetMapping("/consultar/{id}")
    public String consultar(@PathVariable ID id, Model model) {
        return mostrarExistente(id, model, BaseUseCaseController.CONSULTAR);
    }

    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable ID id, Model model) {
        return mostrarExistente(id, model, BaseUseCaseController.MODIFICAR);
    }

    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute("item") T entity,
                             BindingResult resultado,
                             @RequestParam(value = "archivos", required = false) List<MultipartFile> archivos,
                             RedirectAttributes attributes,
                             Model model) {
        try {
            preUseCase(BaseUseCaseController.ACTUALIZAR, model, entity);
            if (resultado.hasErrors()) {
                throw new ErrorServiceException("Hay datos con formato inválido. Revisá el formulario.");
            }
            ejecutarActualizacion(entity, archivos);
            postUseCase(BaseUseCaseController.ACTUALIZAR, model, entity);
            attributes.addFlashAttribute("msgExito", "La acción fue realizada correctamente.");
            return redirectList;
        } catch (ErrorServiceException e) {
            return showErrorMenssage(e.getMessage(), BaseUseCaseController.ACTUALIZAR, model, entity);
        } catch (Exception e) {
            log.error("Error en actualizar", e);
            return showErrorMenssage("Error de Sistemas", BaseUseCaseController.ACTUALIZAR, model, entity);
        }
    }

    /** Baja logica. Es POST (no GET) para que no se pueda disparar con solo abrir un link. */
    @PostMapping("/baja/{id}")
    public String eliminar(@PathVariable ID id, RedirectAttributes attributes, HttpSession sesion) {
        try {
            ejecutarBaja(id, sesion);
            attributes.addFlashAttribute("msgExito", "El registro fue eliminado correctamente.");
        } catch (ErrorServiceException e) {
            attributes.addFlashAttribute("msgError", e.getMessage());
        } catch (Exception e) {
            log.error("Error en baja", e);
            attributes.addFlashAttribute("msgError", "Error de Sistemas");
        }
        return redirectList;
    }

    @GetMapping("/cancelar")
    public String cancelar() {
        return redirectList;
    }

    // ===================== Auxiliares =====================

    private String mostrarExistente(ID id, Model model, BaseUseCaseController useCase) {
        T entity = null;
        try {
            entity = service.obtenerEntidad(id);
            preUseCase(useCase, model, entity);
            return showForm(model, entity, useCase == BaseUseCaseController.CONSULTAR);
        } catch (ErrorServiceException e) {
            return showErrorMenssage(e.getMessage(), BaseUseCaseController.LISTAR, model, entity);
        } catch (Exception e) {
            log.error("Error al mostrar registro", e);
            return showErrorMenssage("Error de Sistemas", BaseUseCaseController.LISTAR, model, entity);
        }
    }

    private String showForm(Model model, T entity, boolean soloLectura) {
        model.addAttribute("item", entity);
        model.addAttribute("isDisabled", soloLectura);
        model.addAttribute("titleEdit", titleEdit);
        model.addAttribute("basePath", basePath);
        model.addAttribute("nameEntityLower", nameEntityLower);
        return viewEdit;
    }

    /** Muestra el mensaje de error: en el listado si fallo una consulta, o en el formulario si fallo un guardado. */
    private String showErrorMenssage(String mensaje, BaseUseCaseController useCase, Model model, T entity) {
        model.addAttribute("msgError", mensaje);
        model.addAttribute("basePath", basePath);
        model.addAttribute("nameEntityLower", nameEntityLower);

        if (useCase == BaseUseCaseController.LISTAR || entity == null) {
            model.addAttribute("titleList", titleList);
            if (!model.containsAttribute("items")) {
                try {
                    model.addAttribute("items", service.listarActivos());
                } catch (Exception ignorada) {
                    model.addAttribute("items", List.of());
                }
            }
            return viewList;
        }

        T aMostrar = entity;
        if (!esNuevo(entity)) {
            try {
                aMostrar = service.obtenerEntidad(entity.getId()); // si ya existe, se vuelve a mostrar el registro guardado
            } catch (Exception ignorada) {
                // si no se puede recargar, se muestra lo que envio el usuario
            }
        }
        try {
            preUseCase(useCase, model, aMostrar); // vuelve a cargar los combos del formulario
        } catch (Exception e) {
            log.warn("No se pudieron recargar los datos del formulario", e);
        }
        return showForm(model, aMostrar, useCase == BaseUseCaseController.CONSULTAR);
    }

    protected boolean esNuevo(T entity) {
        ID id = entity.getId();
        return id == null || (id instanceof String texto && texto.isBlank());
    }
}
