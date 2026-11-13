package com.ejercicioIntegrador.dulcemayor.service.presupuesto;

/** Falla al consultar el modelo de lenguaje. limite=true cuando la API rechazó por cuota o exceso de llamadas. */
public class ErrorProveedorIA extends Exception {

    private final boolean limite;

    public ErrorProveedorIA(String mensaje, boolean limite, Throwable causa) {
        super(mensaje, causa);
        this.limite = limite;
    }

    public boolean isLimite() {
        return limite;
    }
}
