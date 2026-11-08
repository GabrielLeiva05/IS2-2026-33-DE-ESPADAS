package com.ejercicioIntegrador.dulcemayor.service.facturacion;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoFactura;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Factura;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Patrón FACTORY METHOD — CREADOR abstracto (equivale a EnvioCreator del ejemplo del equipo).
 *
 * Problema que resuelve: Dulce Mayor emite dos clases de factura del diagrama, FacturaCliente (a partir de un pedido
 * confirmado) y FacturaProveedor (con los datos que carga administración). Las dos comparten el procedimiento
 * —renglones, subtotal, IVA discriminado, total, estado inicial— pero cada una se instancia y se completa distinto.
 *
 *  - emitir(datos) es la operación común (como procesarEnvio()): no sabe qué subclase de Factura se crea.
 *  - crearFactura(datos) es el MÉTODO FÁBRICA (como crearEnvio()): cada creador concreto instancia su producto.
 *  - renglones(datos), descuento(datos) y fechaFactura(datos) son los otros pasos que varían.
 *
 * Productos: Factura (abstracto) → FacturaCliente, FacturaProveedor.
 * Creadores concretos: CreadorFacturaCliente, CreadorFacturaProveedor.
 * Para una nueva clase de factura se agrega una subclase de Factura y su creador, sin tocar esta clase ni a quien la usa.
 *
 * El creador solo ARMA la factura: no la numera ni la guarda (eso lo hace el servicio de cada factura con su
 * repositorio, dentro del Template Method de BaseService) y no genera el PDF (GeneradorPdfFactura).
 *
 * @param <D> datos de origen de la factura (el pedido, o los datos cargados de la factura de proveedor)
 */
public abstract class CreadorFactura<D> {

    private final double alicuotaIva;

    protected CreadorFactura(double alicuotaIva) {
        if (alicuotaIva < 0 || alicuotaIva >= 1) {
            throw new IllegalArgumentException("La alícuota de IVA debe estar entre 0 y 1 (por ejemplo, 0.21)");
        }
        this.alicuotaIva = alicuotaIva;
    }

    /** MÉTODO FÁBRICA: instancia el producto concreto con sus datos propios (pedido, proveedor y número…). */
    protected abstract Factura crearFactura(D datos) throws MiException;

    /** Renglones a facturar, con el precio unitario sin IVA. */
    protected abstract List<RenglonFactura> renglones(D datos) throws MiException;

    /** Descuento antes del IVA. Por defecto no hay. */
    protected double descuento(D datos) {
        return 0;
    }

    /** Fecha de la factura. Por defecto, ahora. */
    protected LocalDateTime fechaFactura(D datos) {
        return LocalDateTime.now();
    }

    /**
     * Operación común: arma la factura completa. Subtotal = suma de renglones; base = subtotal − descuento;
     * IVA = base × alícuota (discriminado); total = base + IVA. La factura nace PENDIENTE_PAGO.
     */
    public final Factura emitir(D datos) throws MiException {
        Factura factura = crearFactura(datos);
        List<RenglonFactura> renglones = renglones(datos);
        if (renglones == null || renglones.isEmpty()) {
            throw new MiException("La factura debe tener al menos un renglón");
        }
        for (RenglonFactura renglon : renglones) {
            if (renglon.producto() == null || renglon.cantidad() <= 0 || renglon.precioUnitario() <= 0) {
                throw new MiException("Cada renglón necesita un producto, una cantidad y un precio mayores a cero");
            }
            factura.agregarDetalle(renglon.producto(), renglon.cantidad(), redondear(renglon.precioUnitario()));
        }
        double base = redondear(factura.getSubtotal() - descuento(datos));
        if (base <= 0) {
            throw new MiException("El importe a facturar debe ser mayor a cero");
        }
        double iva = redondear(base * alicuotaIva);
        factura.setTotal(redondear(base + iva));
        factura.setFechaFactura(fechaFactura(datos));
        factura.setEstado(EstadoFactura.PENDIENTE_PAGO);
        return factura;
    }

    public double getAlicuotaIva() {
        return alicuotaIva;
    }

    protected static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
