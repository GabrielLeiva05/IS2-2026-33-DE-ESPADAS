package com.tienda.carrito.controller.view;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/** Pagina de error unica (reemplaza la "Whitelabel Error Page"). Se usa para 403, 404, 413, 500, etc. */
@Controller
public class FalloController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object estado = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int codigo = (estado instanceof Integer) ? (Integer) estado : 500;
        model.addAttribute("codigo", codigo);
        model.addAttribute("mensaje", mensajePara(codigo));
        return "view/error";
    }

    private String mensajePara(int codigo) {
        switch (codigo) {
            case 400: return "La solicitud no es válida.";
            case 403: return "No tenés permisos para acceder a esta página.";
            case 404: return "La página que buscás no existe.";
            case 413: return "El archivo supera el tamaño máximo permitido.";
            default:  return "Ocurrió un error inesperado. Probá nuevamente.";
        }
    }
}
