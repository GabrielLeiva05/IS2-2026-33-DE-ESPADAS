package com.tienda.carrito.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/** Muestra una pagina de error amigable cuando se supera el tamanio maximo de subida (se produce antes de llegar al controlador). */
@ControllerAdvice
public class ManejadorGlobalExcepciones {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String archivoMuyGrande(Model model, HttpServletResponse respuesta) {
        respuesta.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        model.addAttribute("codigo", 413);
        model.addAttribute("mensaje", "El archivo supera el tamaño máximo permitido (2 MB por imagen).");
        return "view/error";
    }
}
