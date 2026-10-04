package ar.com.biblioteca.client.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

/** Muestra una página de error amigable cuando falla una llamada a la API. */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ModelAndView api(ApiException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatus());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(status);
        mav.addObject("estado", status.value());
        mav.addObject("mensaje", ex.getMessage());
        return mav;
    }

    /** El PDF elegido supera el límite configurado en spring.servlet.multipart.* */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView archivoDemasiadoGrande(MaxUploadSizeExceededException ex) {
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(HttpStatus.valueOf(413));
        mav.addObject("estado", 413);
        mav.addObject("mensaje", "El archivo seleccionado supera el tamaño máximo permitido (20 MB). "
                + "Vuelva atrás y elija un PDF más liviano.");
        return mav;
    }
}
