package com.ejercicioIntegrador.dulcemayor.service.correo;

/**
 * PATRÓN DECORATOR — COMPONENTE.
 *
 * Interfaz común del componente concreto (el que envía por SMTP) y de los decoradores que lo envuelven.
 * Quien envía correos (ServicioCorreo, el CLIENTE del patrón) solo conoce esta interfaz: no sabe cuántas
 * capas hay ni en qué orden.
 *
 * Estructura en Dulce Mayor (ver ConfiguracionCorreo):
 * <pre>
 *   ComponenteCorreo                              (Componente)
 *    ├─ ComponenteConcretoCorreoSmtp              (Componente Concreto: envía por SMTP)
 *    └─ DecoradorBaseCorreo                       (Decorador Base: delega en el envuelto)
 *        ├─ DecoradorConcretoCorreoHabilitado     (no envía si el correo está deshabilitado)
 *        ├─ DecoradorConcretoCorreoTolerante      (una falla no interrumpe la operación que lo dispara)
 *        └─ DecoradorConcretoCorreoReintentos     (reintenta las fallas transitorias)
 * </pre>
 */
public interface ComponenteCorreo {

    /** Envía el mensaje. Ante una falla lanza ErrorEnvioCorreoException (salvo que un decorador la absorba). */
    void enviar(MensajeCorreo mensaje);
}
