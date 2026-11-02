package com.ejercicioIntegrador.dulcemayor.controller.base;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.OperacionNoPermitidaException;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Patron Template Method para la capa de controladores REST.
 *
 * Cada endpoint sigue el mismo algoritmo:
 *  1. verificar que el caso de uso este habilitado para el recurso (paso redefinible),
 *  2. delegar en el servicio enviando y recibiendo DTO,
 *  3. armar la respuesta HTTP con el codigo correspondiente al caso de uso
 *     (200 consulta/modificacion, 201 + Location en el alta, 204 en la baja).
 *
 * Los controladores concretos solo declaran su @RequestMapping, pasan su servicio y,
 * si un recurso no admite algun caso de uso, redefinen operacionHabilitada().
 * Los errores no se capturan aqui: los traduce ManejadorErrores a una respuesta HTTP.
 *
 * @param <REQ> DTO de entrada
 * @param <RES> DTO de salida
 */
public abstract class BaseController<REQ, RES extends BaseResponseDTO> {

    protected final BaseService<?, REQ, RES> servicio;

    protected BaseController(BaseService<?, REQ, RES> servicio) {
        this.servicio = servicio;
    }

    // ------------------------------------------------------------------
    // Metodos plantilla (endpoints comunes)
    // ------------------------------------------------------------------

    @GetMapping
    public ResponseEntity<List<RES>> listarActivos() throws MiException {
        verificarOperacion(BaseUseCaseController.LISTAR);
        return ResponseEntity.ok(servicio.listarActivos());
    }

    @GetMapping("/todos")
    public ResponseEntity<List<RES>> listar() throws MiException {
        verificarOperacion(BaseUseCaseController.LISTAR);
        return ResponseEntity.ok(servicio.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RES> consultar(@PathVariable String id) throws MiException {
        verificarOperacion(BaseUseCaseController.CONSULTAR);
        return ResponseEntity.ok(servicio.consultar(id));
    }

    @PostMapping
    public ResponseEntity<RES> alta(@RequestBody REQ dto) throws MiException {
        verificarOperacion(BaseUseCaseController.ALTA);
        RES creado = servicio.alta(dto);
        return ResponseEntity.created(ubicacionDe(creado)).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RES> modificar(@PathVariable String id, @RequestBody REQ dto) throws MiException {
        verificarOperacion(BaseUseCaseController.MODIFICACION);
        return ResponseEntity.ok(servicio.modificar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> baja(@PathVariable String id) throws MiException {
        verificarOperacion(BaseUseCaseController.BAJA);
        servicio.baja(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Pasos del algoritmo
    // ------------------------------------------------------------------

    private void verificarOperacion(BaseUseCaseController casoDeUso) throws OperacionNoPermitidaException {
        if (!operacionHabilitada(casoDeUso)) {
            throw new OperacionNoPermitidaException(
                    "La operación " + casoDeUso + " no está disponible para este recurso");
        }
    }

    /**
     * Paso redefinible: indica si el recurso admite el caso de uso.
     * Por defecto todos los casos de uso estan habilitados.
     */
    protected boolean operacionHabilitada(BaseUseCaseController casoDeUso) {
        return true;
    }

    /** URI del recurso creado, relativa a la URL del POST: /api/recurso/{id}. */
    protected URI ubicacionDe(RES creado) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();
    }
}
