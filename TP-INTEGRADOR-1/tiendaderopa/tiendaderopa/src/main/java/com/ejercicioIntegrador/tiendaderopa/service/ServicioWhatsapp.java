package com.ejercicioIntegrador.tiendaderopa.service;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

/**
 * Arma links para abrir WhatsApp Web con un mensaje
 * precargado. No tiene Repository ni depende de otros Services: es un
 * helper de construcción de texto.
 * Arma el link para abrir WhatsApp Web con un mensaje precargado.
 */
@Service
public class ServicioWhatsapp {

    private static final String URL_WHATSAPP_WEB = "https://web.whatsapp.com/send";
    private static final String PREFIJO_CELULAR_ARGENTINA = "549";

    /**
     * Deja el teléfono solo con dígitos y con código de país.
     * Simplificación para Argentina: si no arranca con "54", se le
     * antepone "549" (celular). Se asume que el número está cargado sin
     * el 15 (ej.: 2615551234). Lo ideal es validar y normalizar el
     * teléfono cuando se da de alta el proveedor.
     */
    public String normalizarTelefono(String telefono) {
        if (telefono == null) {
            return null;
        }
        String digitos = telefono.replaceAll("\\D", "");
        if (digitos.isEmpty()) {
            return null;
        }
        if (digitos.startsWith("54")) {
            return digitos;
        }
        return PREFIJO_CELULAR_ARGENTINA + digitos.replaceFirst("^0+", "");
    }

    /**
     * @return la URL de WhatsApp Web con el chat abierto y el mensaje
     *         precargado, o null si no hay teléfono.
     */
    public String armarLinkWhatsappWeb(String telefonoNormalizado, String mensaje) {
        if (telefonoNormalizado == null || telefonoNormalizado.isBlank()) {
            return null;
        }
        return URL_WHATSAPP_WEB
                + "?phone=" + telefonoNormalizado
                + "&text=" + UriUtils.encode(mensaje, StandardCharsets.UTF_8);
    }
}