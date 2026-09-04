package com.example.securityDemo.controladores;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorControlador implements ErrorController {

    @RequestMapping("/error")
    public String renderErrorPage(HttpServletRequest request, ModelMap modelo) {

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        int statusCode = 500;
        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
        }

        String mensajeError;

        switch (statusCode) {
            case 400:
                mensajeError = "La solicitud enviada no es válida o contiene datos incorrectos.";
                break;
            case 401:
                mensajeError = "No estás autenticado para realizar esta acción.";
                break;
            case 403:
                mensajeError = "No tienes los permisos necesarios para acceder a este recurso.";
                break;
            case 404:
                mensajeError = "El recurso o la página que estás buscando no fue encontrada.";
                break;
            case 405:
                mensajeError = "El método HTTP solicitado no está permitido en esta ruta.";
                break;
            case 500:
                mensajeError = "Ocurrió un error interno en el servidor al procesar la solicitud.";
                break;
            default:
                mensajeError = "Ocurrió un error inesperado al procesar tu solicitud.";
                break;
        }

        // Si el contenedor capturó un mensaje puntual y no está vacío, lo agregamos
        if (message != null && !message.toString().isEmpty()) {
            modelo.put("detalle", message.toString());
        } else if (exception != null && exception instanceof Throwable) {
            modelo.put("detalle", ((Throwable) exception).getMessage());
        }

        modelo.put("status", statusCode);
        modelo.put("message", mensajeError);

        return "error.html";
    }
}