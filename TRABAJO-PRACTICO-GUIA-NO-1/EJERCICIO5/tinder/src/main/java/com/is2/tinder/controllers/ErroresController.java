package com.is2.tinder.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErroresController {
    @RequestMapping("/error")
    public ModelAndView error(HttpServletRequest request) {
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        int code = status instanceof Integer ? (Integer) status : 500;
        ModelAndView view = new ModelAndView("error");
        view.addObject("codigo", code);
        view.addObject("mensaje", "Ocurrió un error al procesar la solicitud");
        return view;
    }
}